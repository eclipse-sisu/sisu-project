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
package org.eclipse.sisu.plexus.scanners;

import java.lang.reflect.Modifier;

import org.eclipse.sisu.reflect.ClassSpace;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class CloningClassLoader
    extends ClassLoader
{
    private static final String CLONE_MARKER = "$__plexus";

    private final ClassSpace space;

    CloningClassLoader( final ClassSpace space )
    {
        this.space = space;
    }

    @Override
    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
        throws ClassNotFoundException
    {
        if ( !name.contains( CLONE_MARKER ) )
        {
            try
            {
                return space.loadClass( name );
            }
            catch ( final TypeNotPresentException e )
            {
                throw new ClassNotFoundException( name );
            }
        }
        return super.loadClass( name, resolve );
    }

    static String proxyName( final String realName, final int cloneId )
    {
        final StringBuilder buf = new StringBuilder();
        if ( realName.startsWith( "java" ) )
        {
            buf.append( '$' );
        }
        return buf.append( realName ).append( CloningClassLoader.CLONE_MARKER ).append( cloneId ).toString();
    }

    static String getRealName( final String proxyName )
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

    @Override
    protected Class<?> findClass( final String name )
        throws ClassNotFoundException
    {
        final String proxyName = name.replace( '.', '/' );
        final String superName = getRealName( proxyName );

        if ( superName.equals( proxyName ) )
        {
            throw new ClassNotFoundException( name );
        }

        final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
        cw.visit( Opcodes.V1_5, Modifier.PUBLIC, proxyName, null, superName, null );
        final MethodVisitor mv = cw.visitMethod( Modifier.PUBLIC, "<init>", "()V", null, null );

        mv.visitCode();
        mv.visitVarInsn( Opcodes.ALOAD, 0 );
        mv.visitMethodInsn( Opcodes.INVOKESPECIAL, superName, "<init>", "()V" );
        mv.visitInsn( Opcodes.RETURN );
        mv.visitMaxs( 0, 0 );
        mv.visitEnd();
        cw.visitEnd();

        final byte[] buf = cw.toByteArray();

        return defineClass( name, buf, 0, buf.length );
    }

    @Override
    public String toString()
    {
        return space.toString();
    }
}
