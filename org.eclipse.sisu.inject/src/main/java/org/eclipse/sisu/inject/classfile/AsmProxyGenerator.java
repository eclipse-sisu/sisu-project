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
package org.eclipse.sisu.inject.classfile;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.inject.Provider;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * {@link ProxyGenerator} implementation based on the ASM library.
 */
final class AsmProxyGenerator implements ProxyGenerator {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String PROVIDER_NAME = Type.getInternalName(Provider.class);

    private static final String PROVIDER_DESC = Type.getDescriptor(Provider.class);

    private static final String OBJECT_NAME = Type.getInternalName(Object.class);

    private static final String OBJECT_DESC = Type.getDescriptor(Object.class);

    private static final String ILLEGAL_STATE_NAME = Type.getInternalName(IllegalStateException.class);

    // ----------------------------------------------------------------------
    // Operations
    // ----------------------------------------------------------------------

    @Override
    public byte[] generateClone(String proxyName, String superName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_6, Modifier.PUBLIC, proxyName, null, superName, null);
        MethodVisitor mv = cw.visitMethod(Modifier.PUBLIC, "<init>", "()V", null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();

        return cw.toByteArray();
    }

    @Override
    public byte[] generateProviderProxy(String proxyName, Class<?> facade) {
        String facadeName = Type.getInternalName(facade);

        String superName;
        String[] apiNames;

        if (facade.isInterface()) {
            superName = OBJECT_NAME;
            apiNames = new String[] {facadeName};
        } else {
            superName = facadeName;
            apiNames = getInternalNames(facade.getInterfaces());
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Modifier.PUBLIC | Modifier.FINAL, proxyName, null, superName, apiNames);
        init(cw, superName, proxyName);

        for (final Method m : WrappableMethods.getWrappableMethods(facade)) {
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

        final boolean isObjectMethod = WrappableMethods.isObjectMethod(method);

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
}
