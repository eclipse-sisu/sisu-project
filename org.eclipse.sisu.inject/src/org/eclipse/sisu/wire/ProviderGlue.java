/*******************************************************************************
 * Copyright (c) 2008, 2015 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.eclipse.sisu.space.asm.ClassWriter;
import org.eclipse.sisu.space.asm.Label;
import org.eclipse.sisu.space.asm.MethodVisitor;
import org.eclipse.sisu.space.asm.Opcodes;
import org.eclipse.sisu.space.asm.Type;

final class ProviderGlue
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String PROVIDER_NAME = Type.getInternalName( Provider.class );

    private static final String PROVIDER_DESC = Type.getDescriptor( Provider.class );

    private static final String OBJECT_NAME = Type.getInternalName( Object.class );

    private static final String OBJECT_DESC = Type.getDescriptor( Object.class );

    private static final String ILLEGAL_STATE_EX = Type.getInternalName( IllegalStateException.class );

    private static final String PROXY_SUFFIX = "$sisuglu";

    private static final String PROXY_HANDLE = "__sisu__";

    private static final Map<String, Method> OBJECT_METHOD_MAP;

    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        OBJECT_METHOD_MAP = new HashMap<String, Method>();
        for ( final Method m : Object.class.getMethods() )
        {
            if ( isWrappable( m ) )
            {
                OBJECT_METHOD_MAP.put( methodKey( m ), m );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private ProviderGlue()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static String getProxyName( final String clazzName )
    {
        final StringBuilder tmpName = new StringBuilder();

        // support proxy of java.* interfaces by changing the package space
        if ( clazzName.startsWith( "java." ) || clazzName.startsWith( "java/" ) )
        {
            tmpName.append( '$' );
        }

        return tmpName.append( clazzName ).append( PROXY_SUFFIX ).toString();
    }

    public static String getClazzName( final String proxyName )
    {
        final int head = '$' == proxyName.charAt( 0 ) ? 1 : 0;
        final int tail = proxyName.lastIndexOf( PROXY_SUFFIX );

        return tail > 0 ? proxyName.substring( head, tail ) : proxyName;
    }

    public static byte[] generateProxy( final Class<?> clazz )
    {
        final String clazzName = Type.getInternalName( clazz );
        final String proxyName = getProxyName( clazzName );

        final String superName;
        final String[] interfaceNames;

        if ( clazz.isInterface() )
        {
            superName = OBJECT_NAME;
            interfaceNames = new String[] { clazzName };
        }
        else
        {
            superName = clazzName;
            interfaceNames = getInternalNames( clazz.getInterfaces() );
        }

        final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );

        cw.visit( Opcodes.V1_6, Modifier.PUBLIC | Modifier.FINAL, proxyName, null, superName, interfaceNames );

        // single Provider<T> constructor
        init( cw, superName, proxyName );

        // for the moment only proxy the public API...
        for ( final Method m : getWrappableMethods( clazz ) )
        {
            wrap( cw, proxyName, m );
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static void init( final ClassWriter cw, final String superName, final String proxyName )
    {
        cw.visitField( Modifier.PRIVATE | Modifier.FINAL, PROXY_HANDLE, PROVIDER_DESC, null, null ).visitEnd();

        final MethodVisitor v = cw.visitMethod( Modifier.PUBLIC, "<init>", '(' + PROVIDER_DESC + ")V", null, null );

        v.visitCode();

        // store Provider<T> handle
        v.visitVarInsn( Opcodes.ALOAD, 0 );
        v.visitInsn( Opcodes.DUP );
        v.visitVarInsn( Opcodes.ALOAD, 1 );
        v.visitFieldInsn( Opcodes.PUTFIELD, proxyName, PROXY_HANDLE, PROVIDER_DESC );
        v.visitMethodInsn( Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false );
        v.visitInsn( Opcodes.RETURN );

        v.visitMaxs( 0, 0 );
        v.visitEnd();
    }

    private static void wrap( final ClassWriter cw, final String proxyName, final Method method )
    {
        final String methodName = method.getName();

        final String descriptor = Type.getMethodDescriptor( method );
        final String[] exceptions = getInternalNames( method.getExceptionTypes() );

        // simple delegating proxy, so don't need synchronization on wrapper method
        final int modifiers = method.getModifiers() & ~( Modifier.ABSTRACT | Modifier.NATIVE | Modifier.SYNCHRONIZED );

        final MethodVisitor v = cw.visitMethod( modifiers, methodName, descriptor, null, exceptions );

        v.visitCode();

        // store handle as "this"
        v.visitVarInsn( Opcodes.ALOAD, 0 );
        v.visitFieldInsn( Opcodes.GETFIELD, proxyName, PROXY_HANDLE, PROVIDER_DESC );
        v.visitInsn( Opcodes.DUP );
        v.visitVarInsn( Opcodes.ASTORE, 0 );

        // dereference handle to get actual service instance
        v.visitMethodInsn( Opcodes.INVOKEINTERFACE, PROVIDER_NAME, "get", "()" + OBJECT_DESC, true );
        v.visitInsn( Opcodes.DUP );

        final Label invokeDelegate = new Label();

        // null => ServiceUnavailableException
        v.visitJumpInsn( Opcodes.IFNONNULL, invokeDelegate );
        v.visitTypeInsn( Opcodes.NEW, ILLEGAL_STATE_EX );
        v.visitInsn( Opcodes.DUP );
        v.visitMethodInsn( Opcodes.INVOKESPECIAL, ILLEGAL_STATE_EX, "<init>", "()V", false );
        v.visitInsn( Opcodes.ATHROW );

        v.visitLabel( invokeDelegate );

        final Class<?> clazz = method.getDeclaringClass();
        final String subjectName = Type.getInternalName( clazz );

        if ( !clazz.isInterface() )
        {
            v.visitTypeInsn( Opcodes.CHECKCAST, subjectName );
        }

        int i = 1;
        for ( final Type t : Type.getArgumentTypes( method ) )
        {
            v.visitVarInsn( t.getOpcode( Opcodes.ILOAD ), i );
            i = i + t.getSize();
        }

        // delegate to real method
        if ( clazz.isInterface() )
        {
            v.visitMethodInsn( Opcodes.INVOKEINTERFACE, subjectName, methodName, descriptor, true );
        }
        else
        {
            v.visitMethodInsn( Opcodes.INVOKEVIRTUAL, subjectName, methodName, descriptor, false );
        }

        v.visitInsn( Type.getReturnType( method ).getOpcode( Opcodes.IRETURN ) );

        v.visitMaxs( 0, 0 );
        v.visitEnd();
    }

    private static String[] getInternalNames( final Class<?>... clazzes )
    {
        final String[] names = new String[clazzes.length];
        for ( int i = 0; i < names.length; i++ )
        {
            names[i] = Type.getInternalName( clazzes[i] );
        }
        return names;
    }

    private static Collection<Method> getWrappableMethods( final Class<?> clazz )
    {
        final Map<String, Method> methodMap = new HashMap<String, Method>( OBJECT_METHOD_MAP );
        for ( final Method m : clazz.getMethods() )
        {
            if ( isWrappable( m ) )
            {
                methodMap.put( methodKey( m ), m );
            }
        }
        return methodMap.values();
    }

    private static boolean isWrappable( final Method method )
    {
        return ( method.getModifiers() & ( Modifier.STATIC | Modifier.FINAL ) ) == 0;
    }

    private static String methodKey( final Method method )
    {
        final StringBuilder buf = new StringBuilder( method.getName() );
        for ( final Class<?> t : method.getParameterTypes() )
        {
            buf.append( ':' ).append( t );
        }
        return buf.toString();
    }
}
