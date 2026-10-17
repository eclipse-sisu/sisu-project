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

/**
 * Selects the class file implementation ({@link ClassScanner} and
 * {@link ProxyGenerator}) used by the rest of Sisu.
 *
 * <p>
 * The implementation is selected by the {@value #PROPERTY} system property:
 * {@code asm} (the default) or {@code classfile} (requires JDK 25 or later).
 * Selection happens once, on first use, so changing the property at runtime
 * has no effect.
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
        final String impl = System.getProperty(PROPERTY, ASM);
        if (ASM.equals(impl)) {
            scanner = new AsmClassScanner();
            proxyGenerator = new AsmProxyGenerator();
        } else if (CLASSFILE.equals(impl)) {
            scanner = new JdkClassScanner();
            proxyGenerator = new JdkProxyGenerator();
        } else {
            throw new IllegalArgumentException("Unknown " + PROPERTY + " implementation: '" + impl + "' (supported: "
                    + String.join(", ", SUPPORTED) + ")");
        }
    }
}
