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
package org.eclipse.sisu.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.inject.Binding;
import java.lang.annotation.Annotation;
import org.eclipse.sisu.BaseTests;
import org.eclipse.sisu.Hidden;
import org.junit.jupiter.api.Test;

@BaseTests
class HiddenSourceTest {
    @Test
    void testAnnotationType() {
        final HiddenSource source = new HiddenSource(null);
        assertEquals(Hidden.class, source.annotationType());
    }

    @Test
    void testHashCode() {
        final HiddenSource source = new HiddenSource(null);
        assertEquals(0, source.hashCode());
    }

    @Test
    void testEquals() {
        final HiddenSource sourceA = new HiddenSource(null);
        final HiddenSource sourceB = new HiddenSource("other");

        assertEquals(sourceA, sourceB);
        assertNotEquals(null, sourceA);
        assertNotEquals("not hidden", sourceA);
    }

    @Test
    void testToStringWithSource() {
        final Object owningSource = new Object() {
            @Override
            public String toString() {
                return "MySource";
            }
        };
        final HiddenSource source = new HiddenSource(owningSource);
        assertEquals("MySource", source.toString());
    }

    @Test
    void testToStringWithoutSource() {
        final HiddenSource source = new HiddenSource(null);
        assertEquals("@" + Hidden.class.getName(), source.toString());
    }

    @Test
    void testGetAnnotationForHidden() {
        final HiddenSource source = new HiddenSource(null);
        final Hidden annotation = source.getAnnotation(null, Hidden.class);
        assertSame(source, annotation);
    }

    @Test
    void testGetAnnotationForOtherTypeReturnsNull() {
        final HiddenSource source = new HiddenSource(null);
        assertNull(source.getAnnotation(null, Override.class));
    }

    @Test
    void testGetAnnotationDelegatesWhenSourceIsAnnotatedSource() {
        final AnnotatedSource delegate = new AnnotatedSource() {
            @Override
            public <T extends Annotation> T getAnnotation(final Binding<?> binding, final Class<T> annotationType) {
                return null;
            }
        };
        final HiddenSource source = new HiddenSource(delegate);
        assertNull(source.getAnnotation(null, Override.class));
    }
}
