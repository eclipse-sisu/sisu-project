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

import java.util.Arrays;
import java.util.List;

import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.SisuExtensions;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Providers;

/**
 * Guice module that uses classpath-scanning and auto-wiring to bind JSR330 components from OSGi bundles.
 */
public class BundleModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /**
     * Local bundle classes and resources.
     */
    protected final BundleClassSpace space;

    /**
     * Local bundle extensions to Sisu.
     */
    protected final SisuExtensions extensions;

    /**
     * Shared locator of bound components.
     */
    protected final MutableBeanLocator locator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BundleModule( final Bundle bundle, final MutableBeanLocator locator )
    {
        space = new BundleClassSpace( bundle );
        extensions = SisuExtensions.local( space );
        this.locator = locator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        // apply auto-wiring analysis across all bindings from this bundle
        new WireModule( modules() ).with( extensions ).configure( binder );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Returns the list of configured binding modules for this bundle.
     * 
     * @return The bundle's modules
     */
    protected List<Module> modules()
    {
        return Arrays.asList( extensionsModule(), contextModule(), spaceModule() );
    }

    /**
     * Returns a module that installs modules from {@code META-INF/services/com.google.inject.Module}.
     * 
     * @return Local extensions module
     */
    protected Module extensionsModule()
    {
        return new ExtensionsModule();
    }

    /**
     * Returns a module containing common context bindings for the bundle.
     * 
     * @return Common context module
     */
    protected Module contextModule()
    {
        return new ContextModule();
    }

    /**
     * Returns a module that scans the bundle classpath for components.
     * 
     * @return Classpath scanning module
     */
    protected Module spaceModule()
    {
        return new SpaceModule( space, BeanScanning.select( System.getProperties() ) ).with( extensions );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Guice module that installs modules listed in {@code META-INF/services/com.google.inject.Module}.
     */
    final class ExtensionsModule
        implements Module
    {
        public void configure( final Binder binder )
        {
            extensions.install( binder, Bundle.class, space.getBundle() );
        }
    }

    /**
     * Guice module that provides common context bindings for the bundle.
     */
    final class ContextModule
        implements Module
    {
        public void configure( final Binder binder )
        {
            // wrap locator inside provider to disable auto-registration of injector
            // because we will be registering it explicitly via the bundle publisher
            binder.bind( MutableBeanLocator.class ).toProvider( Providers.of( locator ) );

            final Bundle bundle = space.getBundle();

            binder.bind( ParameterKeys.PROPERTIES ).toInstance( System.getProperties() );
            binder.bind( BundleContext.class ).toInstance( bundle.getBundleContext() );
        }
    }
}
