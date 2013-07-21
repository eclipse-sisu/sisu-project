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
 * SPI mechanism for discovering {@link Module} and Strategy extensions.
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
     * Installs modules listed in {@code META-INF/services/com.google.inject.Module}; modules must have a public no-arg
     * constructor.
     * 
     * @param binder The current binder
     */
    public <T> void install( final Binder binder )
    {
        install( binder, null, null );
    }

    /**
     * Installs modules listed in {@code META-INF/services/com.google.inject.Module}; modules must either have a public
     * no-arg constructor or one with the context type.
     * 
     * @param binder The current binder
     * @param contextType Optional context type
     * @param context Optional context instance
     */
    public <T> void install( final Binder binder, final Class<T> contextType, final T context )
    {
        final String index = "META-INF/services/" + Module.class.getName();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                Object instance = null;
                final Class<?> impl = space.loadClass( name );
                if ( null != contextType )
                {
                    try
                    {
                        instance = impl.getConstructor( contextType ).newInstance( context );
                    }
                    catch ( final NoSuchMethodException e )
                    {
                        // fall-back to default constructor
                    }
                }
                binder.install( (Module) ( null != instance ? instance : impl.newInstance() ) );
            }
            catch ( final Exception e )
            {
                Logs.trace( "Problem installing: {}", name, e );
            }
            catch ( final LinkageError e )
            {
                Logs.trace( "Problem installing: {}", name, e );
            }
        }
    }

    /**
     * {@link WireModule} strategy that lets {@code META-INF/services/org.eclipse.sisu.wire.Wiring} override the default
     * wiring.
     * 
     * @param binder The binder
     * @return Extended wiring
     */
    public Wiring wiring( final Binder binder )
    {
        final List<Wiring> customWiring = load( Wiring.class, binder );
        final Wiring defaultWiring = WireModule.Strategy.DEFAULT.wiring( binder );
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
     * {@link SpaceModule} strategy that lets {@code META-INF/services/org.eclipse.sisu.space.SpaceVisitor} override the
     * default scanning.
     * 
     * @param binder The binder
     * @return Extended visitor
     */
    public SpaceVisitor visitor( final Binder binder )
    {
        final List<SpaceVisitor> customVisitors = load( SpaceVisitor.class, binder );
        final SpaceVisitor defaultVisitor = SpaceModule.Strategy.DEFAULT.visitor( binder );
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Loads {@link Binder} extensions from {@code META-INF/services/ fully-qualified-SPI-name} ; implementations must
     * have a public constructor that takes a single binder argument.
     * 
     * @param spi The extension SPI
     * @param binder The current binder
     * @return List of binder extensions
     */
    private <T> List<T> load( final Class<T> spi, final Binder binder )
    {
        final List<T> extensions = new ArrayList<T>();
        final String index = "META-INF/services/" + spi.getName();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                extensions.add( spi.cast( space.loadClass( name ).getConstructor( Binder.class ).newInstance( binder ) ) );
            }
            catch ( final Exception e )
            {
                Logs.trace( "Problem loading: {}", name, e );
            }
            catch ( final LinkageError e )
            {
                Logs.trace( "Problem loading: {}", name, e );
            }
        }
        return extensions;
    }
}
