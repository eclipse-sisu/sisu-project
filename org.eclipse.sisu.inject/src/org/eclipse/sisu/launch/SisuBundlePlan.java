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

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * {@link BundlePlan} that publishes bundles containing JSR330 components.
 */
public class SisuBundlePlan
    implements BundlePlan
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final MutableBeanLocator locator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuBundlePlan( final MutableBeanLocator locator )
    {
        this.locator = locator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean appliesTo( final Bundle bundle )
    {
        final String imports = (String) bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        if ( null != imports )
        {
            return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
        }
        return false; // doesn't import any interesting packages
    }

    public BindingPublisher publish( final Bundle bundle )
    {
        return new InjectorPublisher( inject( bundle ) );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Creates an {@link Injector} from the bundle's {@link Module} configuration.
     * 
     * @param bundle The bundle
     * @return Bundle injector
     */
    protected Injector inject( final Bundle bundle )
    {
        return Guice.createInjector( compose( bundle ) );
    }

    /**
     * Composes a {@link Module} that configures components from the given bundle.
     * 
     * @param bundle The bundle
     * @return Bundle module
     */
    protected Module compose( final Bundle bundle )
    {
        return new BundleModule( bundle, locator );
    }
}
