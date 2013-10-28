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
package org.eclipse.sisu.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.BindingSubscriber;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.inject.Weak;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * {@link BundleTracker} that tracks JSR330 bundles and uses {@link BundleModule} to bind components.
 */
public class BundleScanner
    extends BundleTracker
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final BindingPublisher PENDING_PUBLISHER = new BindingPublisher()
    {
        public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
        {
            // no-op, publisher is pending
        }

        public <T> void subscribe( final BindingSubscriber<T> subscriber )
        {
            // no-op, publisher is pending
        }

        public int maxBindingRank()
        {
            return Integer.MIN_VALUE;
        }
    };

    private static final String GUICE_SYMBOLIC_NAME = FrameworkUtil.getBundle( Guice.class ).getSymbolicName();

    private static final String SISU_SYMBOLIC_NAME = FrameworkUtil.getBundle( SisuExtender.class ).getSymbolicName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Map<Long, BindingPublisher> publishers =
        Collections.synchronizedMap( Weak.<Long, BindingPublisher> values() );

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
        if ( !needsScanning( bundle ) )
        {
            return null; // don't bother tracking this bundle
        }
        @SuppressWarnings( "boxing" )
        final Long bundleId = bundle.getBundleId();
        if ( null == publishers.get( bundleId ) )
        {
            // placeholder in case we re-enter this method
            publishers.put( bundleId, PENDING_PUBLISHER );
            final BindingPublisher publisher = publishBundle( bundle );
            publishers.put( bundleId, publisher );

            if ( null != publisher )
            {
                // rank publisher based on highest rank it may assign
                locator.add( publisher, publisher.maxBindingRank() );
            }
        }
        return bundle;
    }

    @Override
    public final void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        if ( unpublishBundle( bundle ) )
        {
            @SuppressWarnings( "boxing" )
            final Long bundleId = bundle.getBundleId();
            locator.remove( publishers.remove( bundleId ) );
        }
    }

    /**
     * Purges any previously published bundles that are no longer valid.
     */
    public final void purgeBundles()
    {
        for ( final Long bundleId : new ArrayList<Long>( publishers.keySet() ) )
        {
            @SuppressWarnings( "boxing" )
            final Bundle bundle = context.getBundle( bundleId );
            if ( null == bundle || unpublishBundle( bundle ) )
            {
                locator.remove( publishers.remove( bundleId ) );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Inspects the bundle to see if it deserves in-depth scanning.
     * 
     * @param bundle The candidate bundle
     * @return {@code true} if the bundle should be scanned; otherwise {@code false}
     */
    protected boolean needsScanning( final Bundle bundle )
    {
        final String symbolicName = bundle.getSymbolicName();
        if ( SISU_SYMBOLIC_NAME.equals( symbolicName ) || GUICE_SYMBOLIC_NAME.equals( symbolicName ) )
        {
            return false; // we know these bundles won't require any scanning
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
            return false; // doesn't import any interesting injection packages
        }
        return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
    }

    /**
     * Creates a new {@link BindingPublisher} for the given bundle.
     * 
     * @param bundle The bundle to publish
     * @return New publisher of bindings
     */
    protected BindingPublisher publishBundle( final Bundle bundle )
    {
        final Injector injector = createInjector( bundle );
        final RankingFunction ranking = getRanking( injector );
        return new InjectorPublisher( injector, ranking );
    }

    /**
     * Determines if the given bundle should now be unpublished.
     * 
     * @param bundle The published bundle
     * @return {@code true} if the bundle should be unpublished; otherwise {@code false}
     */
    protected boolean unpublishBundle( final Bundle bundle )
    {
        return ( bundle.getState() & stateMask ) == 0;
    }

    /**
     * Creates a new Guice {@link Injector} for the given bundle.
     * 
     * @param bundle The bundle to scan
     * @return New bundle injector
     */
    protected Injector createInjector( final Bundle bundle )
    {
        return Guice.createInjector( new BundleModule( bundle, locator ) );
    }

    /**
     * Returns the chosen {@link RankingFunction} for the bundle.
     * 
     * @param injector The bundle's injector
     * @return Function to rank bindings
     */
    protected RankingFunction getRanking( final Injector injector )
    {
        return injector.getInstance( RankingFunction.class );
    }
}
