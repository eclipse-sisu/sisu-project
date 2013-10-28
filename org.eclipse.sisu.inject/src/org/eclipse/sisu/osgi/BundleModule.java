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

public class BundleModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    protected final BundleClassSpace space;

    protected final SisuExtensions extensions;

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
        new WireModule( modules() ).with( extensions ).configure( binder );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected List<Module> modules()
    {
        return Arrays.asList( contextModule(), spaceModule().with( extensions ) );
    }

    protected Module contextModule()
    {
        return new ContextModule();
    }

    protected SpaceModule spaceModule()
    {
        return new SpaceModule( space, BeanScanning.select( System.getProperties() ) );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    protected class ContextModule
        implements Module
    {
        public void configure( final Binder binder )
        {
            final Bundle bundle = space.getBundle();

            // wrap locator inside provider to disable auto-registration of injector
            // because we will be registering it explicitly via the bundle publisher
            binder.bind( MutableBeanLocator.class ).toProvider( Providers.of( locator ) );

            binder.bind( ParameterKeys.PROPERTIES ).toInstance( System.getProperties() );
            binder.bind( BundleContext.class ).toInstance( bundle.getBundleContext() );

            extensions.install( binder, Bundle.class, bundle );
        }
    }
}
