/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Enumeration;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class DefaultClassFinderTest {
    private void performTest(String pkg, boolean expectedToFindClasses) {
        final DefaultClassFinder finder = pkg == null ? new DefaultClassFinder() : new DefaultClassFinder(pkg);
        final ClassSpace space = new URLClassSpace(getClass().getClassLoader());
        final Enumeration<URL> classes = finder.findClasses(space);
        assertNotNull(classes);
        if (expectedToFindClasses) {
            assertTrue(classes.hasMoreElements());
        } else {
            assertFalse(classes.hasMoreElements());
        }
    }

    @Test
    void testDefaultConstructor() {
        performTest(null, true);
    }

    @Test
    void testPackageConstructorNonRecursive() {
        performTest("org.eclipse.sisu.space", true);
    }

    @Test
    void testPackageConstructorRecursive() {
        performTest("org.eclipse.sisu.space.*", true);
    }

    @Test
    void testEmptyPackage() {
        performTest("org.eclipse.sisu.nonexistent.*", false);
    }
}
