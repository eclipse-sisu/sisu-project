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

public final class SisuExtensions
    implements SpaceModule.Strategy, WireModule.Strategy
{
    private final ClassSpace space;

    private final boolean global;

    private SisuExtensions( final ClassSpace space, final boolean global )
    {
        this.space = space;
        this.global = global;
    }

    public static SisuExtensions local( final ClassSpace space )
    {
        return new SisuExtensions( space, false );
    }

    public static SisuExtensions global( final ClassSpace space )
    {
        return new SisuExtensions( space, true );
    }

    public <T> void install( final Binder binder )
    {
        install( binder, null, null );
    }

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

    private <T> List<T> load( final Class<T> api, final Binder binder )
    {
        final List<T> extensions = new ArrayList<T>();
        final String index = "META-INF/services/" + api.getName();
        for ( final String name : new IndexedClassFinder( index, global ).indexedNames( space ) )
        {
            try
            {
                extensions.add( api.cast( space.loadClass( name ).getConstructor( Binder.class ).newInstance( binder ) ) );
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
