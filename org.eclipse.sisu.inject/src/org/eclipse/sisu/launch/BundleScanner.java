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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Weak;
import org.eclipse.sisu.space.BundleClassSpace;
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
     * Custom strategies; contributed by attaching fragments to extender.
     */
    protected final List<Strategy> strategies;

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
        strategies = SisuExtensions.local( new BundleClassSpace( context.getBundle() ) ).create( Strategy.class );
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
        final Strategy strategy = findStrategy( bundle );
        if ( null != strategy )
        {
            return addBundleInjector( bundle, strategy );
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
    // Public types
    // ----------------------------------------------------------------------

    public interface Strategy
    {
        /**
         * Returns {@code true} if strategy applies to the bundle; otherwise {@code false}.
         */
        boolean matches( Bundle bundle );

        /**
         * Scans the given bundle and returns a {@link Module} of the resulting bindings.
         * 
         * @param bundle The bundle
         * @return Scanned bindings
         */
        Module scan( Bundle bundle );

        /**
         * Default scanning strategy; scan any bundles that contain JSR330 annotated components.
         */
        Strategy DEFAULT = new Strategy()
        {
            public boolean matches( final Bundle bundle )
            {
                final String imports = (String) bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
                if ( null != imports )
                {
                    return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
                }
                return false; // doesn't import any interesting packages
            }

            public Module scan( final Bundle bundle )
            {
                return new BundleModule( bundle );
            }
        };
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Finds the appropriate scanning strategy for the given bundle.
     * 
     * @param bundle The bundle
     * @return The chosen strategy; {@code null} if it shouldn't be scanned
     */
    protected Strategy findStrategy( final Bundle bundle )
    {
        final String symbolicName = bundle.getSymbolicName();
        if ( SUPPORT_BUNDLE_NAMES.contains( symbolicName ) )
        {
            return null; // ignore our main support bundles
        }
        if ( null != bundle.getHeaders().get( Constants.FRAGMENT_HOST ) )
        {
            return null; // fragment, we'll scan it when we process the host
        }
        // check for any attached strategies; latest first
        for ( int i = strategies.size() - 1; i >= 0; i-- )
        {
            final Strategy strategy = strategies.get( i );
            if ( strategy.matches( bundle ) )
            {
                return strategy; // apply custom strategy
            }
        }
        return Strategy.DEFAULT.matches( bundle ) ? Strategy.DEFAULT : null;
    }

    /**
     * Creates a new {@link Injector} for the given bundle.
     * 
     * @param bundle The bundle
     * @param strategy The strategy
     * @return New injector
     */
    protected Injector createInjector( final Bundle bundle, final Strategy strategy )
    {
        // the locatorModule will auto-register the injector as a publisher
        return Guice.createInjector( locatorModule, strategy.scan( bundle ) );
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

    private Object addBundleInjector( final Bundle bundle, final Strategy strategy )
    {
        @SuppressWarnings( "boxing" )
        final Long bundleId = bundle.getBundleId();
        if ( !bundleInjectors.containsKey( bundleId ) )
        {
            // protect against nested activation calls
            bundleInjectors.put( bundleId, PLACEHOLDER );
            final Injector injector = createInjector( bundle, strategy );
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
