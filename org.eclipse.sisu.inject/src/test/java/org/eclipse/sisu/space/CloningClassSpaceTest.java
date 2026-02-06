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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.sisu.BaseTests;
import org.eclipse.sisu.inject.DeferredClass;
import org.junit.jupiter.api.Test;

@BaseTests
class CloningClassSpaceTest {
    public static class SimpleBean {
        public SimpleBean() {
            // no-op
        }
    }

    @Test
    void testCloneClass() throws Exception {
        final URLClassSpace parentSpace = new URLClassSpace(getClass().getClassLoader());
        final CloningClassSpace cloningSpace = new CloningClassSpace(parentSpace);

        final DeferredClass<?> clone1 = cloningSpace.cloneClass(SimpleBean.class.getName());
        assertNotNull(clone1);

        final DeferredClass<?> clone2 = cloningSpace.cloneClass(SimpleBean.class.getName());
        assertNotNull(clone2);

        // each clone should have a unique name
        assertNotEquals(clone1.getName(), clone2.getName());

        // loading the cloned class should produce a valid type
        final Class<?> clonedType = clone1.load();
        assertNotNull(clonedType);
        assertNotNull(clonedType.getDeclaredConstructor().newInstance());
    }

    @Test
    void testOriginalNameWithCloneMarker() {
        final String originalName = CloningClassSpace.originalName("com/example/MyClass$__sisu1");
        assertEquals("com/example/MyClass", originalName);
    }

    @Test
    void testOriginalNameWithoutCloneMarker() {
        final String originalName = CloningClassSpace.originalName("com/example/MyClass");
        assertEquals("com/example/MyClass", originalName);
    }

    @Test
    void testOriginalNameWithJavaPrefix() {
        final String originalName = CloningClassSpace.originalName("$java.lang.Object$__sisu1");
        assertEquals("java.lang.Object", originalName);
    }

    @Test
    void testOriginalNameWithNonSisuMarker() {
        // clone marker followed by non-digits should not be truncated
        final String originalName = CloningClassSpace.originalName("com/example/MyClass$__sisuFoo");
        assertEquals("com/example/MyClass$__sisuFoo", originalName);
    }

    @Test
    void testMultipleClones() throws Exception {
        final URLClassSpace parentSpace = new URLClassSpace(getClass().getClassLoader());
        final CloningClassSpace cloningSpace = new CloningClassSpace(parentSpace);

        final DeferredClass<?> clone1 = cloningSpace.cloneClass(SimpleBean.class.getName());
        final DeferredClass<?> clone2 = cloningSpace.cloneClass(SimpleBean.class.getName());
        final DeferredClass<?> clone3 = cloningSpace.cloneClass(SimpleBean.class.getName());

        // all clones should be loadable
        assertNotNull(clone1.load());
        assertNotNull(clone2.load());
        assertNotNull(clone3.load());

        // all should be different classes
        assertNotEquals(clone1.load(), clone2.load());
        assertNotEquals(clone2.load(), clone3.load());
    }
}
