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

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * OSGi {@link BundleTracker} that tracks component bundles and uses {@link BundlePlan}s to publish them.
 */
public class SisuTracker
    extends BundleTracker
    implements BundlePlan
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
            // protect against repeated activation calls
            bundlePublishers.put( bundleId, PLACEHOLDER );
            final BindingPublisher publisher = prepare( bundle );
            if ( null != publisher )
            {
                addPublisher( bundleId, publisher );
            }
            else
            {
                bundlePublishers.remove( bundleId );
            }
        }
        return bundle;
    }

    @Override
    @SuppressWarnings( "boxing" )
    public final void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        if ( evictBundle( bundle ) )
        {
            removePublisher( bundle.getBundleId() );
        }
    }

    /**
     * Purges any bundles that are no longer valid.
     */
    @SuppressWarnings( "boxing" )
    public final void purgeBundles()
    {
        for ( final long bundleId : new ArrayList<Long>( bundlePublishers.keySet() ) )
        {
            final Bundle bundle = context.getBundle( bundleId );
            if ( null == bundle || evictBundle( bundle ) )
            {
                removePublisher( bundleId );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    public BindingPublisher prepare( final Bundle bundle )
    {
        if ( SUPPORT_BUNDLE_NAMES.contains( bundle.getSymbolicName() ) )
        {
            return null; // ignore our main support bundles
        }
        if ( null != bundle.getHeaders().get( Constants.FRAGMENT_HOST ) )
        {
            return null; // fragment, we'll scan it when we process the host
        }
        // check plans in reverse order
        BindingPublisher publisher = null;
        for ( int i = plans.size() - 1; i >= 0 && null == publisher; i-- )
        {
            publisher = plans.get( i ).prepare( bundle );
        }
        return publisher;
    }

    /**
     * Discovers plans listed locally under {@code META-INF/services/org.eclipse.sisu.launch.BundlePlan} ;
     * implementations must have a public no-arg constructor or one that accepts a {@link MutableBeanLocator}.
     * 
     * @return List of plans
     */
    protected List<BundlePlan> discoverPlans()
    {
        final List<BundlePlan> localPlans = new ArrayList<BundlePlan>();

        localPlans.add( new SisuBundlePlan( locator ) );
        final SisuExtensions extensions = SisuExtensions.local( new BundleClassSpace( context.getBundle() ) );
        localPlans.addAll( extensions.create( BundlePlan.class, MutableBeanLocator.class, locator ) );

        return localPlans;
    }

    /**
     * Determines whether we should remove the {@link BindingPublisher} associated with the given bundle.
     * 
     * @param bundle The bundle
     * @return {@code true} if the publisher should be removed; otherwise {@code false}
     */
    protected boolean evictBundle( final Bundle bundle )
    {
        return ( bundle.getState() & stateMask ) == 0;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void addPublisher( final Long bundleId, final BindingPublisher publisher )
    {
        if ( locator.add( publisher, publisher.maxBindingRank() ) )
        {
            bundlePublishers.put( bundleId, publisher );
        }
        else if ( publisher instanceof InjectorPublisher )
        {
            // injector was auto-published already, so all we can do is track the injector
            bundlePublishers.put( bundleId, ( (InjectorPublisher) publisher ).getInjector() );
        }
    }

    private void removePublisher( final Long bundleId )
    {
        final Object publisher = bundlePublishers.remove( bundleId );
        if ( publisher instanceof BindingPublisher )
        {
            locator.remove( (BindingPublisher) publisher );
        }
        else if ( publisher instanceof Injector )
        {
            // we're tracking an auto-published injector, use temporary wrapper to remove it
            locator.remove( new InjectorPublisher( (Injector) publisher, null /* unused */) );
        }
    }
}
