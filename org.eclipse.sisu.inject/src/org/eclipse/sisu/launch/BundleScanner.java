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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * OSGi {@link BundleTracker} that tracks JSR330 bundles and uses {@link BundleModule} to bind components.
 */
public class BundleScanner
    extends BundleTracker
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

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

    private static final Map<Long, Injector> bundleInjectors =
        Collections.synchronizedMap( new HashMap<Long, Injector>() );

    /**
     * Mask of bundle states being tracked.
     */
    protected final int stateMask;

    /**
     * Shared locator of bound components.
     */
    protected final MutableBeanLocator locator;

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
            @SuppressWarnings( "boxing" )
            final Long bundleId = bundle.getBundleId();
            if ( !bundleInjectors.containsKey( bundleId ) )
            {
                // placeholder in case we re-enter this method while creating the injector
                bundleInjectors.put( bundleId, null );
                final Injector injector = createInjector( bundle );
                bundleInjectors.put( bundleId, injector );
            }
            return bundle;
        }
        return null; // don't bother tracking this bundle
    }

    @Override
    public final void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        @SuppressWarnings( "boxing" )
        final Long bundleId = bundle.getBundleId();
        if ( evictBundle( bundle, bundleInjectors.get( bundleId ) ) )
        {
            destroyInjector( bundle, bundleInjectors.remove( bundleId ) );
        }
    }

    /**
     * Purges any bundles that are no longer valid.
     */
    public final void purgeBundles()
    {
        for ( final Long bundleId : new ArrayList<Long>( bundleInjectors.keySet() ) )
        {
            @SuppressWarnings( "boxing" )
            final Bundle bundle = context.getBundle( bundleId );
            if ( evictBundle( bundle, bundleInjectors.get( bundleId ) ) )
            {
                destroyInjector( bundle, bundleInjectors.remove( bundleId ) );
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
        // the BundleModule will auto-register the injector as a publisher
        return Guice.createInjector( new BundleModule( bundle, locator ) );
    }

    /**
     * Determines whether we should destroy the {@link Injector} of the given bundle.
     * 
     * @param bundle The bundle
     * @param injector The injector
     * @return {@code true} if the injector should be destroyed; otherwise {@code false}
     */
    protected boolean evictBundle( final Bundle bundle, final Injector injector )
    {
        return null == bundle || ( bundle.getState() & stateMask ) == 0; // missing or inactive
    }

    /**
     * Destroys the old {@link Injector} for the given bundle.
     * 
     * @param bundle The bundle
     * @param injector Old injector
     */
    protected void destroyInjector( final Bundle bundle, final Injector injector )
    {
        if ( null != injector ) // might be placeholder
        {
            // this will match against the original auto-registered publisher
            locator.remove( new InjectorPublisher( injector, null /* unused */) );
        }
    }
}
