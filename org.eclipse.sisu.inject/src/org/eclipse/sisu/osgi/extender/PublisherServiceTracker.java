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
package org.eclipse.sisu.osgi.extender;

import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A service tracker customiser used to locate all {@link BindingPublisher} services registered in the
 * OSGi registry.
 */
public class PublisherServiceTracker
    implements ServiceTrackerCustomizer
{

    private final BundleContext context;

    private final MutableBeanLocator locator;

    /**
     * Creates an instance given the extender {@link BundleContext} and the shared {@link BeanLocator}.
     * 
     * @param context the extender bundle {@link BundleContext}
     * @param locator the shared {@link BeanLocator}
     */
    public PublisherServiceTracker( BundleContext context, MutableBeanLocator locator )
    {
        this.context = context;
        this.locator = locator;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService( ServiceReference reference )
    {
        if ( !ModuleBundleTracker.SYMBOLIC_NAME.equals( reference.getProperty( Constants.SERVICE_PID ) ) )
        {
            return null;
        }

        final Object service = context.getService( reference );

        if ( service instanceof BindingPublisher )
        {
            BindingPublisher publisher = (BindingPublisher) service;
            locator.add( publisher, 0 );
            return (BindingPublisher) publisher;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     * java.lang.Object)
     */
    public void removedService( final ServiceReference reference, final Object publisher )
    {
        if ( publisher instanceof BindingPublisher )
        {
            locator.remove( (BindingPublisher) publisher );
        }
        context.ungetService( reference );
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     * java.lang.Object)
     */
    public void modifiedService( ServiceReference reference, Object service )
    {
        // nothing to do
    }

}
