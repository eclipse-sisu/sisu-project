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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

/**
 * Reports the active class file backend and guards the selection logic of
 * {@link ClassFileSupport}; in particular, that the release 25 layer of the
 * jar (Multi-Release JAR) is exercised when it is visible on the test
 * classpath, where the classfile implementation is the default.
 */
@BaseTests
class ClassFileBackendTest {
    @Test
    void backendMatchesSelection() {
        final String impl = System.getProperty(ClassFileSupport.PROPERTY);
        // the Jdk implementations live in the release 25 layer, which a
        // plain classpath directory does not expose; check whether the layer
        // is visible on the test classpath
        final boolean layerVisible = null != getClass().getResource("JdkClassScanner.class");
        if ("classfile".equals(impl) && !layerVisible) {
            assertThrows(IllegalStateException.class, () -> ClassFileSupport.scanner());
            return;
        }
        final Class<?> scanner = ClassFileSupport.scanner().getClass();
        System.out.println("Class file backend: " + scanner.getName() + "; " + ClassFileSupport.PROPERTY + "=" + impl
                + "; release 25 layer visible: " + layerVisible);
        final String expected = "asm".equals(impl)
                ? "AsmClassScanner"
                : "classfile".equals(impl) || layerVisible ? "JdkClassScanner" : "AsmClassScanner";
        assertEquals(expected, scanner.getSimpleName());
    }
}
