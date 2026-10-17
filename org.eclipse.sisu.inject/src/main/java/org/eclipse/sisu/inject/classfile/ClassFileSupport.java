/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject.classfile;

import org.eclipse.sisu.inject.Logs;

/**
 * Selects the class file implementation ({@link ClassScanner} and
 * {@link ProxyGenerator}) used by the rest of Sisu.
 *
 * <p>
 * The implementation is selected by the {@value #PROPERTY} system property:
 * {@code asm} or {@code classfile}; when the property is not set, the
 * {@code classfile} implementation is used on JDK 25 and later and the
 * {@code asm} implementation before. Selection happens once, on first use,
 * so changing the property at runtime has no effect.
 *
 * <p>
 * The classfile implementation is compiled for release 25 and packaged in
 * the {@code META-INF/versions/25} layer of this jar (Multi-Release JAR);
 * on older JDKs the layer is invisible and only the ASM implementation is
 * available. Since this class must remain loadable on JDK 8, it cannot link
 * directly to the release 25 classes and locates them by name instead.
 */
public final class ClassFileSupport {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    /**
     * System property that selects the class file implementation.
     */
    public static final String PROPERTY = "sisu.classfile";

    private static final String ASM = "asm";

    private static final String CLASSFILE = "classfile";

    private static final String[] SUPPORTED = {ASM, CLASSFILE};

    /**
     * Marker type of the {@code java.lang.classfile} API; its presence
     * indicates JDK 25 or later
     */
    private static final String CLASSFILE_API = "java.lang.classfile.ClassFile";

    private static final String JDK_CLASS_SCANNER = "org.eclipse.sisu.inject.classfile.JdkClassScanner";

    private static final String JDK_PROXY_GENERATOR = "org.eclipse.sisu.inject.classfile.JdkProxyGenerator";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static volatile ClassScanner scanner;

    private static volatile ProxyGenerator proxyGenerator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private ClassFileSupport() {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the selected class scanner.
     */
    public static ClassScanner scanner() {
        ClassScanner result = scanner;
        if (null != result) {
            return result;
        }
        synchronized (ClassFileSupport.class) {
            if (null == scanner) {
                select();
            }
            return scanner;
        }
    }

    /**
     * Returns the selected proxy generator.
     */
    public static ProxyGenerator proxyGenerator() {
        ProxyGenerator result = proxyGenerator;
        if (result != null) {
            return result;
        }
        synchronized (ClassFileSupport.class) {
            if (proxyGenerator == null) {
                select();
            }
            return proxyGenerator;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static void select() {
        String impl = System.getProperty(PROPERTY);
        if (ASM.equals(impl)) {
            selectAsm();
        } else if (CLASSFILE.equals(impl)) {
            selectJdk(true);
        } else if (impl != null) {
            throw new IllegalArgumentException("Unknown " + PROPERTY + " implementation: '" + impl + "' (supported: "
                    + String.join(", ", SUPPORTED) + ")");
        } else if (!classfileAvailable() || !selectJdk(false)) {
            selectAsm();
        }
    }

    private static void selectAsm() {
        scanner = new AsmClassScanner();
        proxyGenerator = new AsmProxyGenerator();
    }

    /**
     * Attempts to select the classfile implementation.
     *
     * @param required
     *        if {@code true}, a failure to load the implementation is
     *        reported as an {@link IllegalStateException}; if {@code false},
     *        the failure is reported as a warning and the caller may fall
     *        back to the ASM implementation
     * @return {@code true} if the classfile implementation was selected
     */
    private static boolean selectJdk(boolean required) {
        try {
            scanner = load(JDK_CLASS_SCANNER);
            proxyGenerator = load(JDK_PROXY_GENERATOR);
            return true;
        } catch (final RuntimeException | ReflectiveOperationException | LinkageError e) {
            if (required) {
                throw new IllegalStateException(
                        "The '" + CLASSFILE + "' class file implementation requires JDK 25 " + "or later", e);
            }
            Logs.warn("Problem initializing the classfile implementation, falling back to asm: {}", e, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T load(String className) throws ReflectiveOperationException {
        return (T) Class.forName(className).getDeclaredConstructor().newInstance();
    }

    private static boolean classfileAvailable() {
        try {
            Class.forName(CLASSFILE_API);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
