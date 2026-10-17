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

import org.eclipse.sisu.space.AnnotationVisitor;
import org.eclipse.sisu.space.ClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * {@link ClassScanner} implementation based on the ASM library.
 */
final class AsmClassScanner implements ClassScanner {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int ASM_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Operations
    // ----------------------------------------------------------------------

    @Override
    public void scan(byte[] code, ClassVisitor visitor) {
        if (null != visitor) {
            new ClassReader(code).accept(adapt(visitor), ASM_FLAGS);
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adapts the given {@link ClassVisitor} to its equivalent ASM form.
     *
     * @param _cv The class visitor to adapt
     * @return ASM-compatible class visitor
     */
    private static org.objectweb.asm.ClassVisitor adapt(ClassVisitor _cv) {
        return null == _cv
                ? null
                : new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(
                            int version,
                            int access,
                            String name,
                            String signature,
                            String superName,
                            String[] interfaces) {
                        _cv.enterClass(access, name, superName, interfaces);
                    }

                    @Override
                    public org.objectweb.asm.AnnotationVisitor visitAnnotation(
                            String desc, boolean visible) {
                        AnnotationVisitor _av = _cv.visitAnnotation(desc);
                        return null == _av
                                ? null
                                : new org.objectweb.asm.AnnotationVisitor(Opcodes.ASM9) {
                                    {
                                        _av.enterAnnotation();
                                    }

                                    @Override
                                    public void visit(String name, Object value) {
                                        _av.visitElement(name, normalize(value));
                                    }

                                    @Override
                                    public void visitEnd() {
                                        _av.leaveAnnotation();
                                    }
                                };
                    }

                    @Override
                    public void visitEnd() {
                        _cv.leaveClass();
                    }
                };
    }

    /**
     * Normalizes the given raw ASM element value to the value contract shared by
     * all {@link ClassScanner} implementations: {@link Type} values (enum
     * constants and class literals) are converted to their binary class names,
     * and arrays are normalized element-wise.
     */
    private static Object normalize(Object value) {
        if (value instanceof Type) {
            return ((Type) value).getClassName();
        }
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            Object[] normalized = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                normalized[i] = normalize(array[i]);
            }
            return normalized;
        }
        return value;
    }
}
