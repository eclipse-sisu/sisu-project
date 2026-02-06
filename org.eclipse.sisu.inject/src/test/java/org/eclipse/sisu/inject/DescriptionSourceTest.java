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
import org.eclipse.sisu.Description;
import org.junit.jupiter.api.Test;

@BaseTests
class DescriptionSourceTest {
    @Test
    void testValue() {
        final DescriptionSource source = new DescriptionSource(null, "test description");
        assertEquals("test description", source.value());
    }

    @Test
    void testAnnotationType() {
        final DescriptionSource source = new DescriptionSource(null, "test");
        assertEquals(Description.class, source.annotationType());
    }

    @Test
    void testHashCode() {
        final DescriptionSource sourceA = new DescriptionSource(null, "hello");
        final DescriptionSource sourceB = new DescriptionSource(null, "hello");
        assertEquals(sourceA.hashCode(), sourceB.hashCode());
        assertEquals(127 * "value".hashCode() ^ "hello".hashCode(), sourceA.hashCode());
    }

    @Test
    void testEquals() {
        final DescriptionSource sourceA = new DescriptionSource(null, "hello");
        final DescriptionSource sourceB = new DescriptionSource(null, "hello");
        final DescriptionSource sourceC = new DescriptionSource(null, "other");

        assertEquals(sourceA, sourceB);
        assertNotEquals(sourceA, sourceC);
        assertNotEquals(null, sourceA);
    }

    @Test
    void testToStringWithSource() {
        final Object owningSource = new Object() {
            @Override
            public String toString() {
                return "MySource";
            }
        };
        final DescriptionSource source = new DescriptionSource(owningSource, "desc");
        assertEquals("MySource", source.toString());
    }

    @Test
    void testToStringWithoutSource() {
        final DescriptionSource source = new DescriptionSource(null, "desc");
        assertEquals("@" + Description.class.getName() + "(value=desc)", source.toString());
    }

    @Test
    void testGetAnnotationForDescription() {
        final DescriptionSource source = new DescriptionSource(null, "test");
        final Description annotation = source.getAnnotation(null, Description.class);
        assertSame(source, annotation);
    }

    @Test
    void testGetAnnotationForOtherTypeReturnsNull() {
        final DescriptionSource source = new DescriptionSource(null, "test");
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
        final DescriptionSource source = new DescriptionSource(delegate, "test");
        assertNull(source.getAnnotation(null, Override.class));
    }
}
