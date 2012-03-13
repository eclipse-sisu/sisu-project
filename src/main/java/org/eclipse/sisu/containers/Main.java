/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.containers;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.binders.ParameterKeys;
import org.eclipse.sisu.binders.SpaceModule;
import org.eclipse.sisu.binders.WireModule;
import org.eclipse.sisu.locators.MutableBeanLocator;
import org.eclipse.sisu.reflect.URLClassSpace;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Bootstrap class that creates a static {@link Injector} by scanning the current class-path for beans.
 */
public final class Main
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> properties;

    private final String[] args;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Main( final Map<?, ?> properties, final String... args )
    {
        this.properties = Collections.unmodifiableMap( properties );
        this.args = args;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main( final String... args )
    {
        boot( System.getProperties(), args );
    }

    public static <T> T boot( final Class<T> type, final String... args )
    {
        return boot( System.getProperties(), args ).getInstance( type );
    }

    public static Injector boot( final Map<?, ?> properties, final String... args )
    {
        final BeanScanning scanning = selectScanning( properties );
        final Module app = wire( scanning, new Main( properties, args ) );
        final Injector injector = Guice.createInjector( app );

        return injector;
    }

    public static Module wire( final BeanScanning scanning, final Module... bindings )
    {
        final Module[] modules = new Module[bindings.length + 1];
        System.arraycopy( bindings, 0, modules, 0, bindings.length );

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        modules[bindings.length] = new SpaceModule( new URLClassSpace( tccl ), scanning );

        return new WireModule( modules );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.requestStaticInjection( SisuGuice.class );
        binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );
        binder.bind( ShutdownThread.class ).asEagerSingleton();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Provides
    @Parameters
    String[] parameters()
    {
        return args.clone();
    }

    static BeanScanning selectScanning( final Map<?, ?> properties )
    {
        final String option = (String) properties.get( BeanScanning.class.getName() );
        if ( null == option || option.length() == 0 )
        {
            return BeanScanning.ON;
        }
        for ( final BeanScanning value : BeanScanning.values() )
        {
            if ( value.name().equalsIgnoreCase( option ) )
            {
                return value;
            }
        }
        throw new IllegalArgumentException( "Unknown BeanScanning option: " + option );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class ShutdownThread
        extends Thread
    {
        private final MutableBeanLocator locator;

        @Inject
        ShutdownThread( final MutableBeanLocator locator )
        {
            this.locator = locator;

            Runtime.getRuntime().addShutdownHook( this );
        }

        @Override
        public void run()
        {
            locator.clear();
        }
    }
}
