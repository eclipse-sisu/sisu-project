/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.launch;

import java.util.Collections;
import java.util.Map;

import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Weak;
import org.eclipse.sisu.osgi.ServiceBindings;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * OSGi extender that uses Sisu and Guice to wire up applications from one or more component bundles.<br>
 * To enable it set the OSGi framework property or system property {@code sisu.extender.enabled} to {@code true}.
 */
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class SisuExtender
    implements BundleActivator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final String PROPERTY_KEY_ENABLED = "sisu.extender.enabled";
    
    // track locators (per-extender-bundle) so they can be re-used when possible
    private static final Map<Long, MutableBeanLocator> locators =
        Collections.synchronizedMap( Weak.<Long, MutableBeanLocator> values() );

    /**
     * Tracker of component bundles.
     */
    protected SisuTracker tracker = null;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        if (Boolean.parseBoolean(context.getProperty(PROPERTY_KEY_ENABLED))) {
            tracker = createTracker( context );
            tracker.open();
        }
    }

    public void stop( final BundleContext context )
    {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Returns the mask of bundle states this extender is interested in.
     * 
     * @return State mask
     */
    protected int bundleStateMask()
    {
        return Bundle.STARTING | Bundle.ACTIVE;
    }

    /**
     * Creates a new tracker of component bundles for this extender.
     * 
     * @param context The extender context
     * @return New bundle tracker
     */
    protected SisuTracker createTracker( final BundleContext context )
    {
        return new SisuTracker( context, bundleStateMask(), findLocator( context ) );
    }

    /**
     * Returns a new locator of bound components for this extender.
     * 
     * @param context The extender context
     * @return New bean locator
     */
    protected MutableBeanLocator createLocator( final BundleContext context )
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        locator.add( new ServiceBindings( context ) );
        return locator;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Finds the locator associated with this extender; creates one if none exist.
     * 
     * @param context The extender context
     * @return Associated bean locator
     */
    protected final MutableBeanLocator findLocator( final BundleContext context )
    {
        @SuppressWarnings( "boxing" )
        final Long extenderId = context.getBundle().getBundleId();
        MutableBeanLocator locator = locators.get( extenderId );
        if ( null == locator )
        {
            locators.put( extenderId, locator = createLocator( context ) );
        }
        return locator;
    }
}
