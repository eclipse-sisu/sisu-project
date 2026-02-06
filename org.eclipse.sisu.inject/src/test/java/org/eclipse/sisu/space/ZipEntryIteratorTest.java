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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class ZipEntryIteratorTest {
    @Test
    void testNonJar() throws IOException {
        final Iterator<String> i = new ZipEntryIterator(new URL("file:"));
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testBlankZip() {
        final Iterator<String> i = new ZipEntryIterator(resource("blank.zip"));
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testEmptyZip() {
        final Iterator<String> i = new ZipEntryIterator(resource("empty.zip"));
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testEmptyJar() {
        final Iterator<String> i = new ZipEntryIterator(resource("empty.jar"));
        assertTrue(i.hasNext());
        assertEquals("META-INF/MANIFEST.MF", i.next());
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testSimpleZip() {
        final Iterator<String> i = new ZipEntryIterator(resource("simple.zip"));
        assertEquals("0", i.next());
        assertEquals("a/1", i.next());
        assertEquals("a/b/2", i.next());
        assertEquals("a/b/c/3", i.next());
        assertEquals("4", i.next());
        assertEquals("x/5", i.next());
        assertEquals("x/y/6", i.next());
        assertEquals("7", i.next());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testSimpleJar() {
        final Iterator<String> i = new ZipEntryIterator(resource("simple.jar"));
        assertEquals("META-INF/", i.next());
        assertEquals("META-INF/MANIFEST.MF", i.next());
        assertEquals("0", i.next());
        assertEquals("a/", i.next());
        assertEquals("a/1", i.next());
        assertEquals("a/b/", i.next());
        assertEquals("a/b/2", i.next());
        assertEquals("a/b/c/", i.next());
        assertEquals("a/b/c/3", i.next());
        assertEquals("4", i.next());
        assertEquals("x/", i.next());
        assertEquals("x/5", i.next());
        assertEquals("x/y/", i.next());
        assertEquals("x/y/6", i.next());
        assertEquals("7", i.next());
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testEmbeddedZip() throws MalformedURLException {
        final Iterator<String> i = new ZipEntryIterator(new URL("jar:" + resource("embedded.zip") + "!/simple.zip"));

        assertEquals("0", i.next());
        assertEquals("a/1", i.next());
        assertEquals("a/b/2", i.next());
        assertEquals("a/b/c/3", i.next());
        assertEquals("4", i.next());
        assertEquals("x/5", i.next());
        assertEquals("x/y/6", i.next());
        assertEquals("7", i.next());

        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testBrokenJar() {
        final Iterator<String> i = new ZipEntryIterator(resource("broken.jar"));
        try {
            i.next();
            fail("Expected Exception");
        } catch (final Exception e) {
        }
    }

    @Test
    void testRemoveNotSupported() throws IOException {
        final Iterator<String> i = new ZipEntryIterator(new URL("file:"));
        try {
            i.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
        }
    }

    private static URL resource(final String name) {
        return ZipEntryIteratorTest.class.getResource(name);
    }
}
