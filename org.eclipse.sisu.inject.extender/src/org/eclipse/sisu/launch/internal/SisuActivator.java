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
package org.eclipse.sisu.launch.internal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.osgi.extender.ModuleBundleTracker;
import org.eclipse.sisu.osgi.extender.PublisherServiceTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;

import com.google.inject.Injector;

/**
 * {@link BundleActivator} that maintains a dynamic {@link Injector} graph by scanning bundles as they come and go.
 */
public final class SisuActivator
    implements BundleActivator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static Reference LOCATOR_REF;

    private DefaultBeanLocator locator;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        synchronized ( SisuActivator.class )
        {
            if ( null != LOCATOR_REF )
            {
                locator = (DefaultBeanLocator) LOCATOR_REF.get();
            }
            if ( null == locator )
            {
                LOCATOR_REF = new WeakReference( locator = new DefaultBeanLocator() );
            }
        }

        createTrackers( context );
    }

    private void createTrackers( final BundleContext context )
    {
        serviceTracker = new ServiceTracker( context, BindingPublisher.class.getName(), new PublisherServiceTracker( context, locator ) );
        serviceTracker.open( true );
        bundleTracker = new BundleTracker( context, Bundle.ACTIVE, new ModuleBundleTracker( locator ) );
        bundleTracker.open();
    }

    public void stop( final BundleContext context )
    {
        bundleTracker.close();
        serviceTracker.close();
        locator.clear();
    }

}
