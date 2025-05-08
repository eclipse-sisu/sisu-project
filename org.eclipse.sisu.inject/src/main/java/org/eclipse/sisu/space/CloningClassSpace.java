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
package org.eclipse.sisu.space;

import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.sisu.inject.DeferredClass;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * {@link ClassSpace} that can create multiple (deferred) copies of the same implementation type.
 */
public final class CloningClassSpace
    extends URLClassSpace
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String CLONE_MARKER = "$__sisu";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int cloneCount;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public CloningClassSpace( final ClassSpace parent )
    {
        super( AccessController.doPrivileged( new PrivilegedAction<ClassLoader>()
        {
            public ClassLoader run()
            {
                return new CloningClassLoader( parent );
            }
        } ), null );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public DeferredClass<?> cloneClass( final String name )
    {
        final StringBuilder buf = new StringBuilder();
        if ( name.startsWith( "java" ) )
        {
            buf.append( '$' );
        }
        return deferLoadClass( buf.append( name ).append( CLONE_MARKER ).append( ++cloneCount ).toString() );
    }

    public static String originalName( final String proxyName )
    {
        final int cloneMarker = proxyName.lastIndexOf( CLONE_MARKER );
        if ( cloneMarker < 0 )
        {
            return proxyName;
        }
        for ( int i = cloneMarker + CLONE_MARKER.length(), end = proxyName.length(); i < end; i++ )
        {
            final char c = proxyName.charAt( i );
            if ( c < '0' || c > '9' )
            {
                return proxyName; // belongs to someone else, don't truncate the name
            }
        }
        return proxyName.substring( '$' == proxyName.charAt( 0 ) ? 1 : 0, cloneMarker );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ClassLoader} that can define multiple copies of the same implementation type.
     */
    private static final class CloningClassLoader
        extends ClassLoader
    {
        private final ClassSpace parent;

        CloningClassLoader( final ClassSpace parent )
        {
            this.parent = parent;
        }

        @Override
        public String toString()
        {
            return parent.toString();
        }

        @Override
        protected synchronized Class<?> loadClass( final String name, final boolean resolve )
            throws ClassNotFoundException
        {
            if ( !name.contains( CLONE_MARKER ) )
            {
                try
                {
                    return parent.loadClass( name );
                }
                catch ( final TypeNotPresentException e )
                {
                    throw new ClassNotFoundException( name );
                }
            }
            return super.loadClass( name, resolve );
        }

        @Override
        protected Class<?> findClass( final String name )
            throws ClassNotFoundException
        {
            final String proxyName = name.replace( '.', '/' );
            final String superName = originalName( proxyName );

            if ( superName.equals( proxyName ) )
            {
                throw new ClassNotFoundException( name );
            }

            final ClassWriter cw = new ClassWriter( 0 );
            cw.visit( Opcodes.V1_6, Modifier.PUBLIC, proxyName, null, superName, null );
            final MethodVisitor mv = cw.visitMethod( Modifier.PUBLIC, "<init>", "()V", null, null );

            mv.visitCode();
            mv.visitVarInsn( Opcodes.ALOAD, 0 );
            mv.visitMethodInsn( Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false );
            mv.visitInsn( Opcodes.RETURN );
            mv.visitMaxs( 1, 1 );
            mv.visitEnd();
            cw.visitEnd();

            final byte[] buf = cw.toByteArray();

            return defineClass( name, buf, 0, buf.length );
        }
    }
}
