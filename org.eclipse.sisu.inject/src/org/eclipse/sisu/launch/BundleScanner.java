/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Weak;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * OSGi {@link BundleTracker} that tracks JSR330 bundles and uses {@link BundleModule} to bind components.
 */
public class BundleScanner
    extends BundleTracker
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Object PLACEHOLDER = new Object();

    private static final Set<String> SUPPORT_BUNDLE_NAMES = new HashSet<String>();

    static
    {
        final Class<?>[] supportTypes = { Inject.class, Guice.class, SisuExtender.class };
        for ( final Class<?> type : supportTypes )
        {
            SUPPORT_BUNDLE_NAMES.add( FrameworkUtil.getBundle( type ).getSymbolicName() );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Map<Long, Object> bundleInjectors = Collections.synchronizedMap( Weak.<Long, Object> values() );

    /**
     * Mask of bundle states being tracked.
     */
    protected final int stateMask;

    /**
     * Shared locator of bound components.
     */
    protected final MutableBeanLocator locator;

    /**
     * Module that auto-registers injectors as publishers with the locator.
     */
    protected final Module locatorModule = new Module()
    {
        public void configure( final Binder binder )
        {
            binder.bind( MutableBeanLocator.class ).toInstance( locator );
        }
    };

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BundleScanner( final BundleContext context, final int stateMask, final MutableBeanLocator locator )
    {
        super( context, stateMask, null );
        this.stateMask = stateMask;
        this.locator = locator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public final void open()
    {
        super.open();

        purgeBundles(); // catch-up with any events we've missed
    }

    @Override
    public final Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        if ( injectBundle( bundle ) )
        {
            return addBundleInjector( bundle );
        }
        return null; // don't bother tracking this bundle
    }

    @Override
    public final void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        if ( evictBundle( bundle ) )
        {
            removeBundleInjector( bundle.getBundleId() );
        }
    }

    /**
     * Purges any bundles that are no longer valid.
     */
    public final void purgeBundles()
    {
        for ( final long bundleId : new ArrayList<Long>( bundleInjectors.keySet() ) )
        {
            final Bundle bundle = context.getBundle( bundleId );
            if ( null == bundle || evictBundle( bundle ) )
            {
                removeBundleInjector( bundleId );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Determines whether we should create an {@link Injector} for the given bundle.
     * 
     * @param bundle The bundle
     * @return {@code true} if an injector should be created; otherwise {@code false}
     */
    protected boolean injectBundle( final Bundle bundle )
    {
        final String symbolicName = bundle.getSymbolicName();
        if ( SUPPORT_BUNDLE_NAMES.contains( symbolicName ) )
        {
            return false; // ignore our main support bundles
        }
        final Dictionary<?, ?> headers = bundle.getHeaders();
        final String host = (String) headers.get( Constants.FRAGMENT_HOST );
        if ( null != host )
        {
            return false; // fragment, we'll scan it when we process the host
        }
        final String imports = (String) headers.get( Constants.IMPORT_PACKAGE );
        if ( null == imports )
        {
            return false; // doesn't import any interesting packages
        }
        return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
    }

    /**
     * Creates a new {@link Injector} for the given bundle.
     * 
     * @param bundle The bundle
     * @return New injector
     */
    protected Injector createInjector( final Bundle bundle )
    {
        // the locatorModule will auto-register the injector as a publisher
        return Guice.createInjector( locatorModule, new BundleModule( bundle ) );
    }

    /**
     * Determines whether we should destroy the {@link Injector} of the given bundle.
     * 
     * @param bundle The bundle
     * @return {@code true} if the injector should be destroyed; otherwise {@code false}
     */
    protected boolean evictBundle( final Bundle bundle )
    {
        return ( bundle.getState() & stateMask ) == 0;
    }

    /**
     * Destroys the old {@link Injector} for the bundle.
     * 
     * @param injector Old injector
     */
    protected void destroyInjector( final Injector injector )
    {
        // this will match against the original auto-registered publisher
        locator.remove( new InjectorPublisher( injector, null /* unused */) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Object addBundleInjector( final Bundle bundle )
    {
        @SuppressWarnings( "boxing" )
        final Long bundleId = bundle.getBundleId();
        if ( !bundleInjectors.containsKey( bundleId ) )
        {
            // protect against nested activation calls
            bundleInjectors.put( bundleId, PLACEHOLDER );
            final Injector injector = createInjector( bundle );
            bundleInjectors.put( bundleId, injector );
        }
        return bundle;
    }

    private void removeBundleInjector( final long bundleId )
    {
        @SuppressWarnings( "boxing" )
        final Object injector = bundleInjectors.remove( bundleId );
        if ( injector instanceof Injector )
        {
            destroyInjector( (Injector) injector );
        }
    }
}
