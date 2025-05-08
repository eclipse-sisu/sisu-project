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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.ClassVisitor;
import org.eclipse.sisu.space.IndexedClassFinder;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.SpaceVisitor;
import org.eclipse.sisu.wire.WireModule;
import org.eclipse.sisu.wire.Wiring;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;

/**
 * SPI mechanism for discovering {@link Module} and {@code Strategy} extensions.
 */
public final class SisuExtensions
    implements SpaceModule.Strategy, WireModule.Strategy
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final boolean global;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private SisuExtensions( final ClassSpace space, final boolean global )
    {
        this.space = space;
        this.global = global;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Returns local {@link SisuExtensions} from the containing class space.
     * 
     * @param space The class space
     * @return Local extensions
     */
    public static SisuExtensions local( final ClassSpace space )
    {
        return new SisuExtensions( space, false );
    }

    /**
     * Returns global {@link SisuExtensions} from the surrounding class space.
     * 
     * @param space The class space
     * @return Global extensions
     */
    public static SisuExtensions global( final ClassSpace space )
    {
        return new SisuExtensions( space, true );
    }

    /**
     * Installs modules listed under {@code META-INF/services/com.google.inject.Module}; modules must have a public
     * no-arg constructor.
     * 
     * @param binder The current binder
     */
    public void install( final Binder binder )
    {
        install( binder, null, null );
    }

    /**
     * Installs modules listed under {@code META-INF/services/com.google.inject.Module}; modules must either have a
     * public no-arg constructor or one with the declared context type.
     * 
     * @param binder The current binder
     * @param contextType Optional context type
     * @param context Optional context instance
     */
    public <C> void install( final Binder binder, final Class<C> contextType, final C context )
    {
        for ( final Module m : create( Module.class, contextType, context ) )
        {
            binder.install( m );
        }
    }

    /**
     * {@link WireModule} strategy that lets {@code META-INF/services/org.eclipse.sisu.wire.Wiring} extensions override
     * the default wiring.
     * 
     * @param binder The binder
     * @return Extended wiring
     */
    public Wiring wiring( final Binder binder )
    {
        final Wiring defaultWiring = WireModule.Strategy.DEFAULT.wiring( binder );
        final List<Wiring> customWiring = create( Wiring.class, Binder.class, binder );
        return customWiring.isEmpty() ? defaultWiring : new Wiring()
        {
            public boolean wire( final Key<?> key )
            {
                for ( final Wiring w : customWiring )
                {
                    if ( w.wire( key ) )
                    {
                        return true;
                    }
                }
                return defaultWiring.wire( key );
            }
        };
    }

    /**
     * {@link SpaceModule} strategy that lets {@code META-INF/services/org.eclipse.sisu.space.SpaceVisitor} extensions
     * override the default scanning.
     * 
     * @param binder The binder
     * @return Extended visitor
     */
    public SpaceVisitor visitor( final Binder binder )
    {
        final SpaceVisitor defaultVisitor = SpaceModule.Strategy.DEFAULT.visitor( binder );
        final List<SpaceVisitor> customVisitors = create( SpaceVisitor.class, Binder.class, binder );
        return customVisitors.isEmpty() ? defaultVisitor : new SpaceVisitor()
        {
            public void enterSpace( final ClassSpace _space )
            {
                for ( final SpaceVisitor v : customVisitors )
                {
                    v.enterSpace( _space );
                }
                defaultVisitor.enterSpace( _space );
            }

            public ClassVisitor visitClass( final URL url )
            {
                for ( final SpaceVisitor v : customVisitors )
                {
                    final ClassVisitor cv = v.visitClass( url );
                    if ( null != cv )
                    {
                        return cv;
                    }
                }
                return defaultVisitor.visitClass( url );
            }

            public void leaveSpace()
            {
                for ( final SpaceVisitor v : customVisitors )
                {
                    v.leaveSpace();
                }
                defaultVisitor.leaveSpace();
            }
        };

    }

    /**
     * Creates instances of extensions listed under {@code META-INF/services/ fully-qualified-SPI-name} ;
     * implementations must have a public no-arg constructor.
     * 
     * @param spi The extension SPI
     * @return List of extensions
     */
    public <T> List<T> create( final Class<T> spi )
    {
        return create( spi, null, null );
    }

    /**
     * Creates instances of extensions listed under {@code META-INF/services/ fully-qualified-SPI-name} ;
     * implementations must either have a public no-arg constructor or one with the declared context type.
     * 
     * @param spi The extension SPI
     * @param contextType Optional context type
     * @param context Optional context instance
     * @return List of extensions
     */
    public <T, C> List<T> create( final Class<T> spi, final Class<C> contextType, final C context )
    {
        final List<T> extensions = new ArrayList<T>();
        for ( final Class<? extends T> impl : load( spi ) )
        {
            try
            {
                T instance = null;
                if ( null != contextType )
                {
                    try
                    {
                        instance = impl.getConstructor( contextType ).newInstance( context );
                    }
                    catch ( final NoSuchMethodException e ) // NOPMD
                    {
                        // fall-back to default constructor
                    }
                }
                extensions.add( null != instance ? instance : impl.newInstance() );
            }
            catch ( final Exception e )
            {
                final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
                Logs.debug( "Problem creating: {}", impl, cause );
            }
            catch ( final LinkageError e )
            {
                Logs.debug( "Problem creating: {}", impl, e );
            }
        }
        return extensions;
    }

    /**
     * Loads extension types listed under {@code META-INF/services/ fully-qualified-SPI-name}.
     * 
     * @param spi The extension SPI
     * @return List of extension types
     */
    public <T> List<Class<? extends T>> load( final Class<T> spi )
    {
        final String index = "META-INF/services/" + spi.getName();
        final List<Class<? extends T>> extensionTypes = new ArrayList<Class<? extends T>>();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                extensionTypes.add( space.loadClass( name ).asSubclass( spi ) );
            }
            catch ( final Exception e )
            {
                Logs.debug( "Problem loading: {}", name, e );
            }
            catch ( final LinkageError e )
            {
                Logs.debug( "Problem loading: {}", name, e );
            }
        }
        return extensionTypes;
    }
}
