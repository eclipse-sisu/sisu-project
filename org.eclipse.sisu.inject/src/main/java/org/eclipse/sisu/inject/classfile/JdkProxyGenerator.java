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

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.inject.Provider;

/**
 * {@link ProxyGenerator} implementation based on the {@code java.lang.classfile}
 * API (JDK 25 and later).
 */
final class JdkProxyGenerator implements ProxyGenerator {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final ClassFile BUILDER = ClassFile.of();

    // V1_6, for parity with the ASM implementation
    private static final int MAJOR_VERSION = 50;

    private static final MethodTypeDesc VOID_NO_ARGS = MethodTypeDesc.ofDescriptor("()V");

    private static final ClassDesc PROVIDER_DESC = classDesc(Provider.class);

    private static final ClassDesc OBJECT_DESC = classDesc(Object.class);

    private static final ClassDesc ILLEGAL_STATE_DESC = classDesc(IllegalStateException.class);

    private static final MethodTypeDesc PROVIDER_CONSTRUCTOR =
            MethodTypeDesc.ofDescriptor("(" + PROVIDER_DESC.descriptorString() + ")V");

    // ----------------------------------------------------------------------
    // Operations
    // ----------------------------------------------------------------------

    @Override
    public byte[] generateClone(String proxyName, String superName) {
        ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        ClassEntry proxyEntry = pool.classEntry(ClassDesc.ofInternalName(proxyName));
        ClassDesc superDesc = ClassDesc.ofInternalName(superName);
        return BUILDER.build(proxyEntry, pool, builder -> {
            builder.withVersion(MAJOR_VERSION, 0);
            builder.withFlags(Modifier.PUBLIC);
            builder.withSuperclass(superDesc);
            builder.withMethodBody("<init>", VOID_NO_ARGS, Modifier.PUBLIC, code -> {
                code.aload(0);
                code.invokespecial(superDesc, "<init>", VOID_NO_ARGS);
                code.return_(TypeKind.VOID);
            });
        });
    }

    @Override
    public byte[] generateProviderProxy(String proxyName, Class<?> facade) {
        ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        ClassDesc proxyDesc = ClassDesc.ofInternalName(proxyName);
        ClassEntry proxyEntry = pool.classEntry(proxyDesc);

        ClassDesc facadeDesc = classDesc(facade);
        ClassDesc superDesc;
        ClassDesc[] apiDescs;

        if (facade.isInterface()) {
            superDesc = OBJECT_DESC;
            apiDescs = new ClassDesc[] {facadeDesc};
        } else {
            superDesc = facadeDesc;
            final Class<?>[] ifaces = facade.getInterfaces();
            apiDescs = new ClassDesc[ifaces.length];
            for (int i = 0; i < ifaces.length; i++) {
                apiDescs[i] = classDesc(ifaces[i]);
            }
        }

        return BUILDER.build(proxyEntry, pool, builder -> {
            builder.withVersion(MAJOR_VERSION, 0);
            builder.withFlags(Modifier.PUBLIC | Modifier.FINAL);
            builder.withSuperclass(superDesc);
            if (apiDescs.length > 0) {
                builder.withInterfaceSymbols(apiDescs);
            }
            init(builder, superDesc, proxyDesc);

            for (Method m : WrappableMethods.getWrappableMethods(facade)) {
                wrap(pool, builder, proxyDesc, m);
            }
        });
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Generates a constructor that accepts a {@link Provider} and stores it in an internal field.
     */
    private static void init(ClassBuilder builder, ClassDesc superDesc, ClassDesc proxyDesc) {
        builder.withField(PROVIDER_HANDLE, PROVIDER_DESC, Modifier.PRIVATE | Modifier.FINAL);
        builder.withMethodBody("<init>", PROVIDER_CONSTRUCTOR, Modifier.PUBLIC, code -> {
            code.aload(0);
            code.dup();
            code.aload(1);
            code.fieldAccess(Opcode.PUTFIELD, proxyDesc, PROVIDER_HANDLE, PROVIDER_DESC);
            code.invokespecial(superDesc, "<init>", VOID_NO_ARGS);
            code.return_(TypeKind.VOID);
        });
    }

    /**
     * Generates a wrapper that dereferences the stored {@link Provider} and invokes the given method.
     */
    private static void wrap(
            ConstantPoolBuilder pool,
            ClassBuilder builder,
            ClassDesc proxyDesc,
            Method method) {
        String methodName = method.getName();
        MethodTypeDesc descriptor = methodDescriptor(method);
        Class<?> declaringClazz = method.getDeclaringClass();
        ClassDesc declaringDesc = classDesc(declaringClazz);
        boolean isObjectMethod = WrappableMethods.isObjectMethod(method);
        boolean isInterface = declaringClazz.isInterface();
        Class<?>[] parameterTypes = method.getParameterTypes();

        // simple delegating proxy, so don't need synchronization on wrapper method
        int modifiers = method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.NATIVE | Modifier.SYNCHRONIZED);

        Class<?>[] exceptionTypes = method.getExceptionTypes();
        ClassDesc[] exceptionDescs = new ClassDesc[exceptionTypes.length];
        for (int i = 0; i < exceptionTypes.length; i++) {
            exceptionDescs[i] = classDesc(exceptionTypes[i]);
        }

        builder.withMethod(methodName, descriptor, modifiers, methodBuilder -> {
            if (exceptionDescs.length > 0) {
                methodBuilder.with(ExceptionsAttribute.ofSymbols(exceptionDescs));
            }
            methodBuilder.withCode(code -> {
                Label handleNullTarget = code.newLabel();

                // delegate all non-Object methods as well as 'toString'
                if (!isObjectMethod || "toString".equals(methodName)) {
                    // dereference and check target
                    code.aload(0);
                    code.fieldAccess(Opcode.GETFIELD, proxyDesc, PROVIDER_HANDLE, PROVIDER_DESC);
                    code.invoke(Opcode.INVOKEINTERFACE, PROVIDER_DESC, "get", MethodTypeDesc.of(OBJECT_DESC), true);

                    code.dup();
                    code.branch(Opcode.IFNULL, handleNullTarget);

                    if (!isInterface && Object.class != declaringClazz) {
                        // must check target before setting up INVOKEVIRTUAL
                        code.checkcast(declaringDesc);
                    }

                    loadArguments(code, parameterTypes);

                    // invoke original method on target
                    Opcode invoke = isInterface ? Opcode.INVOKEINTERFACE : Opcode.INVOKEVIRTUAL;
                    code.invoke(invoke, declaringDesc, methodName, descriptor, isInterface);
                    code.return_(typeKind(method.getReturnType()));

                    code.labelBinding(handleNullTarget);
                    code.pop();

                    if (!isObjectMethod) {
                        // no fall-back available, report illegal state
                        code.with(NewObjectInstruction.of(pool.classEntry(ILLEGAL_STATE_DESC)));
                        code.dup();
                        code.invokespecial(ILLEGAL_STATE_DESC, "<init>", VOID_NO_ARGS);
                        code.athrow();
                    }
                }

                if (isObjectMethod) {
                    code.aload(0);
                    loadArguments(code, parameterTypes);

                    // fall-back to superclass implementation for all Object methods
                    code.invokespecial(declaringDesc, methodName, descriptor);
                    code.return_(typeKind(method.getReturnType()));
                }
            });
        });
    }

    /**
     * Loads the given method's arguments from the local variable slots.
     */
    private static void loadArguments(CodeBuilder code, Class<?>[] parameterTypes) {
        int slot = 1;
        for (Class<?> parameterType : parameterTypes) {
            TypeKind kind = typeKind(parameterType);
            code.loadLocal(kind, slot);
            slot += kind.slotSize();
        }
    }

    /**
     * Returns the {@link TypeKind} for the given class.
     */
    private static TypeKind typeKind(Class<?> clazz) {
        return TypeKind.fromDescriptor(clazz.descriptorString());
    }

    /**
     * Returns the {@link MethodTypeDesc} for the given method.
     */
    private static MethodTypeDesc methodDescriptor(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        ClassDesc[] parameterDescs = new ClassDesc[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterDescs[i] = classDesc(parameterTypes[i]);
        }
        return MethodTypeDesc.of(classDesc(method.getReturnType()), parameterDescs);
    }

    /**
     * Returns the {@link ClassDesc} for the given class.
     */
    private static ClassDesc classDesc(Class<?> clazz) {
        return ClassDesc.ofDescriptor(clazz.descriptorString());
    }
}
