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

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attribute;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.List;
import org.eclipse.sisu.space.AnnotationVisitor;
import org.eclipse.sisu.space.ClassVisitor;

/**
 * {@link ClassScanner} implementation based on the {@code java.lang.classfile}
 * API (JDK 25 and later).
 */
final class JdkClassScanner implements ClassScanner {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final ClassFile READER = ClassFile.of();

    // ----------------------------------------------------------------------
    // Operations
    // ----------------------------------------------------------------------

    @Override
    public void scan(byte[] code, ClassVisitor visitor) {
        if (null == visitor) {
            return;
        }
        ClassModel model = READER.parse(code);
        visitor.enterClass(
                model.flags().flagsMask(),
                model.thisClass().asInternalName(),
                model.superclass().map(ClassEntry::asInternalName).orElse(null),
                model.interfaces().stream().map(ClassEntry::asInternalName).toArray(String[]::new));

        for (Attribute<?> attribute : model.attributes()) {
            switch (attribute) {
                case RuntimeVisibleAnnotationsAttribute visible -> visitAnnotations(visible.annotations(), visitor);
                case RuntimeInvisibleAnnotationsAttribute invisible -> visitAnnotations(invisible.annotations(), visitor);
                default -> {}
            }
        }

        visitor.leaveClass();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static void visitAnnotations(List<Annotation> annotations, ClassVisitor cv) {
        for (Annotation annotation : annotations) {
            AnnotationVisitor av = cv.visitAnnotation(annotation.classSymbol().descriptorString());
            if (null != av) {
                av.enterAnnotation();
                for (AnnotationElement element : annotation.elements()) {
                    Object value = normalize(element.value());
                    if (null != value) {
                        av.visitElement(element.name().stringValue(), value);
                    }
                }
                av.leaveAnnotation();
            }
        }
    }

    /**
     * Normalizes the given annotation value to the value contract shared by all
     * {@link ClassScanner} implementations (see
     * {@link AnnotationVisitor#visitElement(String, Object)}).
     */
    private static Object normalize(AnnotationValue value) {
        if (value instanceof AnnotationValue.OfEnum enumValue) {
            return binaryName(enumValue.classSymbol());
        }
        if (value instanceof AnnotationValue.OfClass classValue) {
            return binaryName(classValue.classSymbol());
        }
        if (value instanceof AnnotationValue.OfAnnotation) {
            return null; // nested annotations are not delivered
        }
        if (value instanceof AnnotationValue.OfArray array) {
            return array.values().stream().map(JdkClassScanner::normalize).toArray();
        }
        if (value instanceof AnnotationValue.OfByte byteValue) {
            return byteValue.byteValue();
        }
        if (value instanceof AnnotationValue.OfChar charValue) {
            return charValue.charValue();
        }
        if (value instanceof AnnotationValue.OfShort shortValue) {
            return shortValue.shortValue();
        }
        if (value instanceof AnnotationValue.OfInt intValue) {
            return intValue.intValue();
        }
        if (value instanceof AnnotationValue.OfLong longValue) {
            return longValue.longValue();
        }
        if (value instanceof AnnotationValue.OfFloat floatValue) {
            return floatValue.floatValue();
        }
        if (value instanceof AnnotationValue.OfDouble doubleValue) {
            return doubleValue.doubleValue();
        }
        if (value instanceof AnnotationValue.OfBoolean booleanValue) {
            return booleanValue.booleanValue();
        }
        if (value instanceof AnnotationValue.OfString stringValue) {
            return stringValue.stringValue();
        }
        throw new IllegalArgumentException("Unsupported annotation value: " + value);
    }

    /**
     * Returns the binary name (e.g. {@code com.foo.Bar}) of the given class
     * descriptor.
     */
    private static String binaryName(ClassDesc descriptor) {
        String fieldDescriptor = descriptor.descriptorString();
        return fieldDescriptor.substring(1, fieldDescriptor.length() - 1).replace('/', '.');
    }
}
