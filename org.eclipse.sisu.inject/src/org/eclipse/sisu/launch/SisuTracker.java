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

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Weak;
import org.eclipse.sisu.space.BundleClassSpace;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * OSGi {@link BundleTracker} that tracks component bundles and uses {@link BundlePlan}s to publish them.
 */
public class SisuTracker
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

    // attempt to track publishers across extender restarts
    private static final Map<Long, Object> bundlePublishers =
        Collections.synchronizedMap( Weak.<Long, Object> values() );

    /**
     * Mask of bundle states being tracked.
     */
    protected final int stateMask;

    /**
     * Shared locator of bound components.
     */
    protected final MutableBeanLocator locator;

    /**
     * Custom plans; contributed by attaching fragments to the extender bundle.
     */
    protected final List<BundlePlan> plans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuTracker( final BundleContext context, final int stateMask, final MutableBeanLocator locator )
    {
        super( context, stateMask, null );

        this.stateMask = stateMask;
        this.locator = locator;

        plans = discoverPlans();
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
        @SuppressWarnings( "boxing" )
        final Long bundleId = bundle.getBundleId();
        if ( !bundlePublishers.containsKey( bundleId ) )
        {
            final BundlePlan plan = selectPlan( bundle );
            if ( null != plan )
            {
                // protect against repeated activation calls
                bundlePublishers.put( bundleId, PLACEHOLDER );
                final BindingPublisher publisher = plan.publish( bundle );
                locator.add( publisher, publisher.maxBindingRank() );
                bundlePublishers.put( bundleId, publisher );
            }
        }
        return bundle;
    }

    @Override
    public final void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        if ( evictBundle( bundle ) )
        {
            @SuppressWarnings( "boxing" )
            final Object publisher = bundlePublishers.remove( bundle.getBundleId() );
            if ( publisher instanceof BindingPublisher )
            {
                locator.remove( (BindingPublisher) publisher );
            }
        }
    }

    /**
     * Purges any bundles that are no longer valid.
     */
    public final void purgeBundles()
    {
        for ( final long bundleId : new ArrayList<Long>( bundlePublishers.keySet() ) )
        {
            final Bundle bundle = context.getBundle( bundleId );
            if ( null == bundle || evictBundle( bundle ) )
            {
                @SuppressWarnings( "boxing" )
                final Object publisher = bundlePublishers.remove( bundleId );
                if ( publisher instanceof BindingPublisher )
                {
                    locator.remove( (BindingPublisher) publisher );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Discovers plans listed locally under {@code META-INF/services/org.eclipse.sisu.launch.BundlePlan} ;
     * implementations must have a public no-arg constructor or one that accepts a {@link MutableBeanLocator}.
     * 
     * @return List of plans
     */
    protected List<BundlePlan> discoverPlans()
    {
        final SisuExtensions extensions = SisuExtensions.local( new BundleClassSpace( context.getBundle() ) );
        final List<BundlePlan> localPlans = extensions.create( BundlePlan.class, MutableBeanLocator.class, locator );

        Collections.reverse( localPlans ); // TODO: support prioritized list?
        localPlans.add( new SisuBundlePlan( locator ) );

        return localPlans;
    }

    /**
     * Selects the appropriate plan for the given bundle.
     * 
     * @param bundle The bundle
     * @return The chosen plan
     */
    protected BundlePlan selectPlan( final Bundle bundle )
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
        for ( final BundlePlan plan : plans )
        {
            if ( plan.appliesTo( bundle ) )
            {
                return plan;
            }
        }
        return null; // nothing to do
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
}
