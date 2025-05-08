/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.launch;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.InjectorBindings;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * {@link BundlePlan} that prepares {@link BindingPublisher}s for JSR330 bundles.
 */
public class SisuBundlePlan
    implements BundlePlan
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    protected final MutableBeanLocator locator;

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

    public BindingPublisher prepare( final Bundle bundle )
    {
        return appliesTo( bundle ) ? InjectorBindings.findBindingPublisher( inject( compose( bundle ) ) ) : null;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * @return {@code true} if plan applies to the bundle; otherwise {@code false}
     */
    protected boolean appliesTo( final Bundle bundle )
    {
        if ( bundle.getHeaders().get( "Bundle-Blueprint" ) != null )
        {
            return false;
        }
        final String imports = bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        return null != imports && ( imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" ) );
    }

    /**
     * Creates an {@link Injector} from the composed {@link Module} configuration.
     * 
     * @param module The module
     * @return Bundle injector
     */
    protected Injector inject( final Module module )
    {
        return Guice.createInjector( module );
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
