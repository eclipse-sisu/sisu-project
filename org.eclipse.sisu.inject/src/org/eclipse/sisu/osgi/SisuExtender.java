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

import java.util.Collections;
import java.util.Map;

import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Weak;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi extender that watches for JSR330 bundles and publishes them to the {@link BeanLocator}.<br>
 * Extend this class to customize the publication process and use it as your Bundle-Activator.
 */
public class SisuExtender
    implements BundleActivator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    // track locators (per-extender-bundle) so they can be re-used when possible
    private static final Map<Long, MutableBeanLocator> locators =
        Collections.synchronizedMap( Weak.<Long, MutableBeanLocator> values() );

    private BundleScanner scanner;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        scanner = createScanner( context );
        scanner.open();
    }

    public void stop( final BundleContext context )
    {
        scanner.close();
        scanner = null;
    }

    /**
     * Finds the {@link BeanLocator} associated with this extender; creates one if none exist.
     * 
     * @param context The extender context
     * @return Associated bean locator
     */
    public final MutableBeanLocator findLocator( final BundleContext context )
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
     * Creates a customised {@link BundleScanner} for this extender.
     * 
     * @param context The extender context
     * @return New bundle scanner
     */
    protected BundleScanner createScanner( final BundleContext context )
    {
        return new BundleScanner( context, bundleStateMask(), findLocator( context ) );
    }

    /**
     * Returns a new locator of bound components for this extender.
     * 
     * @param context The extender context
     * @return New bean locator
     */
    protected MutableBeanLocator createLocator( final BundleContext context )
    {
        return new DefaultBeanLocator();
    }
}
