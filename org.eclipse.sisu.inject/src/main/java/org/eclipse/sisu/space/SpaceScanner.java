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
package org.eclipse.sisu.space;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import org.eclipse.sisu.inject.Logs;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Makes a {@link SpaceVisitor} visit a {@link ClassSpace}; can be directed by an optional {@link ClassFinder}.
 */
public final class SpaceScanner {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int ASM_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    static final ClassFinder DEFAULT_FINDER = new DefaultClassFinder();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final ClassFinder finder;

    /**
     * If set to {@code true} will throw {@link RuntimeException} in case class cannot be scanned.
     */
    private final boolean isStrict;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SpaceScanner(final ClassSpace space, final ClassFinder finder, boolean isStrict) {
        this.space = space;
        this.finder = finder;
        this.isStrict = isStrict;
    }

    /**
     * @param space
     * @param finder
     * @deprecated Use {@link #SpaceScanner(ClassSpace, ClassFinder, boolean)} instead.
     */
    @Deprecated
    public SpaceScanner(final ClassSpace space, final ClassFinder finder) {
        this(space, finder, false);
    }

    public SpaceScanner(final ClassSpace space, boolean isStrict) {
        this(space, DEFAULT_FINDER, isStrict);
    }

    /**
     *
     * @param space
     * @deprecated Use {@link #SpaceScanner(ClassSpace, boolean)} instead.
     */
    @Deprecated
    public SpaceScanner(final ClassSpace space) {
        this(space, DEFAULT_FINDER);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Makes the given {@link SpaceVisitor} visit the {@link ClassSpace} of this scanner.
     *
     * @param visitor The class space visitor
     */
    public void accept(final SpaceVisitor visitor) {
        visitor.enterSpace(space);

        for (final Enumeration<URL> result = finder.findClasses(space); result.hasMoreElements(); ) {
            final URL url = result.nextElement();
            final ClassVisitor cv = visitor.visitClass(url);
            if (null != cv) {
                accept(cv, url, isStrict);
            }
        }

        visitor.leaveSpace();
    }

    /**
     * Shortcut for {@link #accept(ClassVisitor, URL, boolean)} with third parameter being {@code false}.
     * @param visitor The class space visitor
     * @param url The class resource URL
     * @deprecated Use {@link #accept(ClassVisitor, URL, boolean)} instead.
     */
    @Deprecated
    public static void accept(final ClassVisitor visitor, final URL url) {
        accept(visitor, url, false);
    }

    /**
     * Makes the given {@link ClassVisitor} visit the class contained in the resource {@link URL}.
     *
     * @param visitor The class space visitor
     * @param url The class resource URL
     * @param isStrict If set to {@code true} throws {@link RuntimeException} in case of parsing issues with the class
     */
    public static void accept(final ClassVisitor visitor, final URL url, boolean isStrict) {
        if (null == url) {
            return; // nothing to visit
        }
        try (final InputStream in = Streams.open(url)) {
            new ClassReader(in).accept(adapt(visitor), ASM_FLAGS);
        } catch (final IOException | RuntimeException e) {
            if (isStrict) {
                throw new IllegalStateException("Problem scanning " + url, e);
            } else {
                Logs.debug("Problem scanning: {}", url, e);
            }
        }
    }

    /**
     * Returns the JVM descriptor for the given annotation class, such as "Ljavax/inject/Qualifier;".
     *
     * @param clazz The annotation class
     * @return JVM descriptor of the class
     * @see ClassVisitor#visitAnnotation(String)
     */
    public static String jvmDescriptor(final Class<? extends Annotation> clazz) {
        return 'L' + clazz.getName().replace('.', '/') + ';';
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
    private static org.objectweb.asm.ClassVisitor adapt(final ClassVisitor _cv) {
        return null == _cv
                ? null
                : new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(
                            final int version,
                            final int access,
                            final String name,
                            final String signature,
                            final String superName,
                            final String[] interfaces) {
                        _cv.enterClass(access, name, superName, interfaces);
                    }

                    @Override
                    public org.objectweb.asm.AnnotationVisitor visitAnnotation(
                            final String desc, final boolean visible) {
                        final AnnotationVisitor _av = _cv.visitAnnotation(desc);
                        return null == _av
                                ? null
                                : new org.objectweb.asm.AnnotationVisitor(Opcodes.ASM9) {
                                    {
                                        _av.enterAnnotation();
                                    }

                                    @Override
                                    public void visit(final String name, final Object value) {
                                        _av.visitElement(
                                                name, value instanceof Type ? ((Type) value).getClassName() : value);
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
}
