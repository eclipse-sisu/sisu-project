/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.eclipse.sisu.wire;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Provider;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Utility methods for generating dynamic {@link Provider}-based proxies.
 */
final class DynamicGlue {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String PROVIDER_NAME = Type.getInternalName(Provider.class);

    private static final String PROVIDER_DESC = Type.getDescriptor(Provider.class);

    private static final String PROVIDER_HANDLE = "__sisu__";

    private static final String OBJECT_NAME = Type.getInternalName(Object.class);

    private static final String OBJECT_DESC = Type.getDescriptor(Object.class);

    private static final String ILLEGAL_STATE_NAME = Type.getInternalName(IllegalStateException.class);

    private static final Map<String, Method> OBJECT_METHOD_MAP;

    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static {
        // pre-seed common methods that should be wrapped
        OBJECT_METHOD_MAP = new HashMap<>();
        for (final Method m : Object.class.getMethods()) {
            if (isWrappable(m)) {
                OBJECT_METHOD_MAP.put(signatureKey(m), m);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private DynamicGlue() {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Generates a dynamic {@link Provider}-based proxy that reflects the given facade.
     *
     * @param proxyName The proxy name
     * @param facade The expected facade
     * @return Generated proxy bytes
     */
    public static byte[] generateProxyClass(final String proxyName, final Class<?> facade) {
        final String facadeName = Type.getInternalName(facade);

        final String superName;
        final String[] apiNames;

        if (facade.isInterface()) {
            superName = OBJECT_NAME;
            apiNames = new String[] {facadeName};
        } else {
            superName = facadeName;
            apiNames = getInternalNames(facade.getInterfaces());
        }

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Modifier.PUBLIC | Modifier.FINAL, proxyName, null, superName, apiNames);
        init(cw, superName, proxyName);

        for (final Method m : getWrappableMethods(facade)) {
            wrap(cw, proxyName, m);
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Generates a constructor that accepts a {@link Provider} and stores it in an internal field.
     */
    private static void init(final ClassWriter cw, final String superName, final String proxyName) {
        cw.visitField(Modifier.PRIVATE | Modifier.FINAL, PROVIDER_HANDLE, PROVIDER_DESC, null, null)
                .visitEnd();

        final MethodVisitor v = cw.visitMethod(Modifier.PUBLIC, "<init>", '(' + PROVIDER_DESC + ")V", null, null);

        v.visitCode();
        v.visitVarInsn(Opcodes.ALOAD, 0);
        v.visitInsn(Opcodes.DUP);
        v.visitVarInsn(Opcodes.ALOAD, 1);
        v.visitFieldInsn(Opcodes.PUTFIELD, proxyName, PROVIDER_HANDLE, PROVIDER_DESC);
        v.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false);
        v.visitInsn(Opcodes.RETURN);
        v.visitMaxs(0, 0);
        v.visitEnd();
    }

    /**
     * Generates a wrapper that dereferences the stored {@link Provider} and invokes the given method.
     */
    private static void wrap(final ClassWriter cw, final String proxyName, final Method method) {
        final String methodName = method.getName();
        final String descriptor = Type.getMethodDescriptor(method);
        final String[] exceptions = getInternalNames(method.getExceptionTypes());
        final Label handleNullTarget = new Label();

        // simple delegating proxy, so don't need synchronization on wrapper method
        final int modifiers = method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.NATIVE | Modifier.SYNCHRONIZED);
        final MethodVisitor v = cw.visitMethod(modifiers, methodName, descriptor, null, exceptions);

        v.visitCode();

        final Class<?> declaringClazz = method.getDeclaringClass();
        final String declaringName = Type.getInternalName(declaringClazz);

        final boolean isObjectMethod = OBJECT_METHOD_MAP.containsKey(signatureKey(method));

        // delegate all non-Object methods as well as 'toString'
        if (!isObjectMethod || "toString".equals(methodName)) {
            // dereference and check target
            v.visitVarInsn(Opcodes.ALOAD, 0);
            v.visitFieldInsn(Opcodes.GETFIELD, proxyName, PROVIDER_HANDLE, PROVIDER_DESC);
            v.visitMethodInsn(Opcodes.INVOKEINTERFACE, PROVIDER_NAME, "get", "()" + OBJECT_DESC, true);

            v.visitInsn(Opcodes.DUP);
            v.visitJumpInsn(Opcodes.IFNULL, handleNullTarget);

            final boolean isInterface = declaringClazz.isInterface();
            if (!isInterface && Object.class != declaringClazz) {
                // must check target before setting up INVOKEVIRTUAL
                v.visitTypeInsn(Opcodes.CHECKCAST, declaringName);
            }

            int slot = 1;
            for (final Type t : Type.getArgumentTypes(method)) {
                v.visitVarInsn(t.getOpcode(Opcodes.ILOAD), slot);
                slot = slot + t.getSize();
            }

            // invoke original method on target
            final int invoke = isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;
            v.visitMethodInsn(invoke, declaringName, methodName, descriptor, isInterface);
            v.visitInsn(Type.getReturnType(method).getOpcode(Opcodes.IRETURN));

            v.visitLabel(handleNullTarget);
            v.visitInsn(Opcodes.POP);

            if (!isObjectMethod) {
                // no fall-back available, report illegal state
                v.visitTypeInsn(Opcodes.NEW, ILLEGAL_STATE_NAME);
                v.visitInsn(Opcodes.DUP);
                v.visitMethodInsn(Opcodes.INVOKESPECIAL, ILLEGAL_STATE_NAME, "<init>", "()V", false);
                v.visitInsn(Opcodes.ATHROW);
            }
        }

        if (isObjectMethod) {
            v.visitVarInsn(Opcodes.ALOAD, 0);

            int slot = 1;
            for (final Type t : Type.getArgumentTypes(method)) {
                v.visitVarInsn(t.getOpcode(Opcodes.ILOAD), slot);
                slot = slot + t.getSize();
            }

            // fall-back to superclass implementation for all Object methods
            v.visitMethodInsn(Opcodes.INVOKESPECIAL, declaringName, methodName, descriptor, false);
            v.visitInsn(Type.getReturnType(method).getOpcode(Opcodes.IRETURN));
        }

        v.visitMaxs(0, 0);
        v.visitEnd();
    }

    /**
     * Returns the internal names of the given classes.
     */
    private static String[] getInternalNames(final Class<?>... clazzes) {
        final String[] names = new String[clazzes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = Type.getInternalName(clazzes[i]);
        }
        return names;
    }

    /**
     * Returns the methods that should be wrapped for delegation in the given class.
     */
    private static Collection<Method> getWrappableMethods(final Class<?> clazz) {
        final Map<String, Method> methodMap = new HashMap<>(OBJECT_METHOD_MAP);
        for (final Method m : clazz.getMethods()) {
            if (isWrappable(m)) {
                methodMap.put(signatureKey(m), m);
            }
        }
        return methodMap.values();
    }

    /**
     * Returns {@code true} if the given method should be wrapped; otherwise {@code false}.
     */
    private static boolean isWrappable(final Method method) {
        return (method.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0;
    }

    /**
     * Returns a signature-based key that identifies the given method in the current class.
     */
    private static String signatureKey(final Method method) {
        final StringBuilder buf = new StringBuilder(method.getName());
        for (final Class<?> t : method.getParameterTypes()) {
            buf.append(':').append(t);
        }
        return buf.toString();
    }
}
