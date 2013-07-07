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
import org.eclipse.sisu.space.AnnotationVisitor;
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
        final List<Wiring> wiringList = load( Wiring.class, binder );
        wiringList.add( WireModule.Strategy.DEFAULT.wiring( binder ) );
        return asWiring( wiringList );
    }

    public SpaceVisitor visitor( final Binder binder )
    {
        final List<SpaceVisitor> visitorList = load( SpaceVisitor.class, binder );
        visitorList.add( SpaceModule.Strategy.DEFAULT.visitor( binder ) );
        return asSpaceVisitor( visitorList );
    }

    <T> List<T> load( final Class<T> api, final Binder binder )
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

    static Wiring asWiring( final List<Wiring> wiring )
    {
        return wiring.size() == 1 ? wiring.get( 0 ) : new Wiring()
        {
            public boolean wire( final Key<?> key )
            {
                for ( final Wiring w : wiring )
                {
                    if ( w.wire( key ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    static SpaceVisitor asSpaceVisitor( final List<SpaceVisitor> visitors )
    {
        return visitors.size() == 1 ? visitors.get( 0 ) : new SpaceVisitor()
        {
            public void enterSpace( final ClassSpace space )
            {
                for ( final SpaceVisitor v : visitors )
                {
                    v.enterSpace( space );
                }
            }

            public ClassVisitor visitClass( final URL url )
            {
                List<ClassVisitor> cvs = null;
                for ( int i = 0, size = visitors.size(); i < size; i++ )
                {
                    final ClassVisitor cv = visitors.get( i ).visitClass( url );
                    if ( null != cv )
                    {
                        if ( null == cvs )
                        {
                            if ( i == size - 1 )
                            {
                                return cv;
                            }
                            cvs = new ArrayList<ClassVisitor>();
                        }
                        cvs.add( cv );
                    }
                }
                return null == cvs ? null : asClassVisitor( cvs );
            }

            public void leaveSpace()
            {
                for ( final SpaceVisitor v : visitors )
                {
                    v.leaveSpace();
                }
            }
        };
    }

    static ClassVisitor asClassVisitor( final List<ClassVisitor> visitors )
    {
        return visitors.size() == 1 ? visitors.get( 0 ) : new ClassVisitor()
        {
            public void enterClass( final int modifiers, final String name, final String _extends,
                                    final String[] _implements )
            {
                for ( final ClassVisitor v : visitors )
                {
                    v.enterClass( modifiers, name, _extends, _implements );
                }
            }

            public AnnotationVisitor visitAnnotation( final String desc )
            {
                List<AnnotationVisitor> avs = null;
                for ( int i = 0, size = visitors.size(); i < size; i++ )
                {
                    final AnnotationVisitor av = visitors.get( i ).visitAnnotation( desc );
                    if ( null != av )
                    {
                        if ( null == avs )
                        {
                            if ( i == size - 1 )
                            {
                                return av;
                            }
                            avs = new ArrayList<AnnotationVisitor>();
                        }
                        avs.add( av );
                    }
                }
                return null == avs ? null : asAnnotationVisitor( avs );
            }

            public void leaveClass()
            {
                for ( final ClassVisitor v : visitors )
                {
                    v.leaveClass();
                }
            }
        };
    }

    static AnnotationVisitor asAnnotationVisitor( final List<AnnotationVisitor> visitors )
    {
        return visitors.size() == 1 ? visitors.get( 0 ) : new AnnotationVisitor()
        {
            public void enterAnnotation()
            {
                for ( final AnnotationVisitor v : visitors )
                {
                    v.enterAnnotation();
                }
            }

            public void visitElement( final String name, final Object value )
            {
                for ( final AnnotationVisitor v : visitors )
                {
                    v.visitElement( name, value );
                }
            }

            public void leaveAnnotation()
            {
                for ( final AnnotationVisitor v : visitors )
                {
                    v.leaveAnnotation();
                }
            }
        };
    }
}
