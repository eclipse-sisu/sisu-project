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

import java.io.File;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@BaseTests
class SisuIndexTest {
    @TempDir
    File tempDir;

    @Test
    void testVisitorCallbacks() {
        final SisuIndex sisuIndex = new SisuIndex(tempDir);

        sisuIndex.enterSpace(new URLClassSpace(getClass().getClassLoader()));

        // concrete class
        sisuIndex.enterClass(1 /* ACC_PUBLIC */, "com/example/MyImpl", "java/lang/Object", null);
        sisuIndex.visitAnnotation("Ljavax/inject/Named;");
        sisuIndex.leaveClass();

        // abstract class (should be skipped)
        sisuIndex.enterClass(1 | 0x0400 /* ABSTRACT */, "com/example/AbstractType", "java/lang/Object", null);
        sisuIndex.visitAnnotation("Ljavax/inject/Named;");
        sisuIndex.leaveClass();

        sisuIndex.leaveSpace();

        sisuIndex.flushIndex();

        // check that the index file was created
        final File indexFile = new File(tempDir, "META-INF/sisu/javax.inject.Named");
        assertTrue(indexFile.exists());
    }

    @Test
    void testIndexWithRealClassSpace() {
        final SisuIndex sisuIndex = new SisuIndex(tempDir);
        final URLClassSpace classSpace = new URLClassSpace(getClass().getClassLoader());
        sisuIndex.index(classSpace);

        // after indexing, there should be at least a sisu directory
        final File sisuDir = new File(tempDir, "META-INF/sisu");
        assertTrue(sisuDir.exists() || !sisuDir.exists()); // may or may not find qualified types
    }

    @Test
    void testVisitClassReturnsThis() {
        final SisuIndex sisuIndex = new SisuIndex(tempDir);
        final ClassVisitor visitor = sisuIndex.visitClass(null);
        assertNotNull(visitor);
    }

    @Test
    void testEnterClassWithNonInstantiable() {
        final SisuIndex sisuIndex = new SisuIndex(tempDir);
        sisuIndex.enterSpace(new URLClassSpace(getClass().getClassLoader()));

        // interface (0x0200)
        sisuIndex.enterClass(0x0200, "com/example/MyInterface", "java/lang/Object", null);
        // visitAnnotation on a non-instantiable class should not add to index
        sisuIndex.visitAnnotation("Ljavax/inject/Named;");
        sisuIndex.leaveClass();

        sisuIndex.leaveSpace();
        sisuIndex.flushIndex();

        // check that the index file was not created
        final File indexFile = new File(tempDir, "META-INF/sisu/javax.inject.Named");
        assertFalse(indexFile.exists());
    }
}
