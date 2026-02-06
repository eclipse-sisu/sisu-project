/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import static org.eclipse.sisu.space.FileEntryIteratorTest.expand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class ResourceEnumerationTest {
    private static final URL COMMONS_LOGGING_JAR = ZipEntryIteratorTest.class.getResource("commons-logging-1.1.1.jar");

    @Test
    void testResourceEnumeration() throws Exception {
        final Enumeration<URL> e =
                new ResourceEnumeration(null, null, true, new URL[] {COMMONS_LOGGING_JAR, expand(COMMONS_LOGGING_JAR)});

        int n = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        assertEquals(66, n);

        try {
            e.nextElement();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException nse) {
        }
    }

    @Test
    void testFixedEnumeration() {
        final Enumeration<URL> e1 = new ResourceEnumeration(
                "org/apache/commons/logging", "LogFactory.clazz", false, new URL[] {COMMONS_LOGGING_JAR});

        assertFalse(e1.hasMoreElements());

        final Enumeration<URL> e2 = new ResourceEnumeration(
                "org/apache/commons/logging", "LogFactory.class", false, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory.class",
                e2.nextElement().getPath());
        assertFalse(e2.hasMoreElements());
    }

    @Test
    void testRecursiveEnumeration() {
        final Enumeration<URL> e =
                new ResourceEnumeration("/", "LogFactory.class", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testGlobbedEnumeration() {
        int n = 0;
        final Enumeration<URL> e = new ResourceEnumeration("/", "*", true, new URL[] {COMMONS_LOGGING_JAR});
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        assertEquals(33, n);
    }

    @Test
    void testGlobbedEnumerationStart() {
        final Enumeration<URL> e = new ResourceEnumeration(null, "*$2.class", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$2.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testGlobbedEnumerationEnd() {
        final Enumeration<URL> e = new ResourceEnumeration(null, "SimpleLog.*", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/impl/SimpleLog.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testGlobbedEnumerationMiddle() {
        final Enumeration<URL> e =
                new ResourceEnumeration(null, "LogFactory*.class", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$3.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$4.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$5.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$6.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testMultiGlobbedEnumeration() {
        final Enumeration<URL> e = new ResourceEnumeration(null, "*Fact*$*", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$3.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$4.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$5.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$6.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testSubPathEnumeration() {
        final Enumeration<URL> e = new ResourceEnumeration(
                "/org/apache/commons/logging/impl", "*Fact*$*", true, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/impl/LogFactoryImpl$3.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testNonRecursiveSubPathEnumeration() {
        final Enumeration<URL> e = new ResourceEnumeration(
                "/org/apache/commons/logging/", "*Fact*$*", false, new URL[] {COMMONS_LOGGING_JAR});

        final String prefix = COMMONS_LOGGING_JAR + "!/";
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$1.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$2.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$3.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$4.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$5.class",
                e.nextElement().getPath());
        assertEquals(
                prefix + "org/apache/commons/logging/LogFactory$6.class",
                e.nextElement().getPath());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void testBrokenUrlEnumeration() throws Exception {
        final Enumeration<URL> e = new ResourceEnumeration(null, null, true, new URL[] {expand(COMMONS_LOGGING_JAR)});

        assertTrue(e.hasMoreElements());

        // intentionally break the internal next entry name to trigger a malformed URL problem
        final Field nextEntryName = ResourceEnumeration.class.getDeclaredField("nextEntryName");
        nextEntryName.setAccessible(true);
        nextEntryName.set(e, "foo:");

        assertThrows(IllegalStateException.class, () -> {
            e.nextElement();
        });
    }
}
