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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class FileEntryIteratorTest {
    @Test
    void testURLtoFile() throws MalformedURLException {
        assertEquals("test", FileEntryIterator.toFile(new URL("file:test")).getPath());
        assertEquals("A B C", FileEntryIterator.toFile(new URL("file:A B C")).getPath());
        assertEquals(
                "A B C%%", FileEntryIterator.toFile(new URL("file:A B%20C%%")).getPath());
        assertEquals("A%B%C%", FileEntryIterator.toFile(new URL("file:A%B%C%")).getPath());
        assertEquals("A+%+C", FileEntryIterator.toFile(new URL("file:A+%+C")).getPath());
    }

    @Test
    void testNoSuchFile() throws Exception {
        final Iterator<String> i = new FileEntryIterator(new URL("file:UNKNOWN"), "", true);
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
        }
    }

    @Test
    void testEmptyFolder() throws Exception {
        final Iterator<String> i = new FileEntryIterator(expand(resource("empty.zip")), "", true);
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
        }
    }

    @Test
    void testTrivialFolder() throws Exception {
        final Iterator<String> i = new FileEntryIterator(expand(resource("empty.jar")), "", true);
        assertTrue(i.hasNext());
        assertEquals("META-INF/", i.next());
        assertTrue(i.hasNext());
        assertEquals("META-INF/MANIFEST.MF", i.next());
        assertFalse(i.hasNext());
        try {
            i.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
        }
    }

    @Test
    void testSimpleFolder() throws Exception {
        final Iterator<String> i = new FileEntryIterator(expand(resource("simple.jar")), "", true);

        final Set<String> names = new HashSet<>();
        while (i.hasNext()) {
            names.add(i.next());
        }

        assertTrue(names.remove("META-INF/"));
        assertTrue(names.remove("META-INF/MANIFEST.MF"));
        assertTrue(names.remove("0"));
        assertTrue(names.remove("a/"));
        assertTrue(names.remove("a/1"));
        assertTrue(names.remove("a/b/"));
        assertTrue(names.remove("a/b/2"));
        assertTrue(names.remove("a/b/c/"));
        assertTrue(names.remove("a/b/c/3"));
        assertTrue(names.remove("4"));
        assertTrue(names.remove("x/"));
        assertTrue(names.remove("x/5"));
        assertTrue(names.remove("x/y/"));
        assertTrue(names.remove("x/y/6"));
        assertTrue(names.remove("7"));

        assertTrue(names.isEmpty());
    }

    @Test
    void testNoRecursion() throws Exception {
        final Iterator<String> i = new FileEntryIterator(expand(resource("simple.jar")), "", false);

        final Set<String> names = new HashSet<>();
        while (i.hasNext()) {
            names.add(i.next());
        }

        assertTrue(names.remove("META-INF/"));
        assertTrue(names.remove("0"));
        assertTrue(names.remove("a/"));
        assertTrue(names.remove("4"));
        assertTrue(names.remove("x/"));
        assertTrue(names.remove("7"));

        assertTrue(names.isEmpty());
    }

    @Test
    void testSubPath() throws Exception {
        final Iterator<String> i = new FileEntryIterator(expand(resource("simple.jar")), "a/b", true);

        final Set<String> names = new HashSet<>();
        while (i.hasNext()) {
            names.add(i.next());
        }

        assertTrue(names.remove("a/b/2"));
        assertTrue(names.remove("a/b/c/"));
        assertTrue(names.remove("a/b/c/3"));

        assertTrue(names.isEmpty());
    }

    @Test
    void testRemoveNotSupported() throws IOException {
        final Iterator<String> i = new FileEntryIterator(new URL("file:"), "", false);
        try {
            i.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
        }
    }

    static URL expand(final URL url) throws Exception {
        final File jar = new File(url.toURI());
        final File dir = new File(jar.getParentFile(), jar.getName() + "_expanded");

        try (final ZipFile zip = new ZipFile(jar)) {
            for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                final ZipEntry entry = e.nextElement();
                final File path = new File(dir, entry.getName());
                if (!path.toPath().normalize().startsWith(dir.toPath().normalize())) {
                    throw new IOException("Bad zip entry");
                }
                if (entry.isDirectory()) {
                    path.mkdirs();
                } else {
                    path.getParentFile().mkdirs();
                    final ReadableByteChannel in = Channels.newChannel(zip.getInputStream(entry));
                    final FileOutputStream os = new FileOutputStream(path);
                    os.getChannel().transferFrom(in, 0, entry.getSize());
                    os.close();
                    in.close();
                }
            }
        } catch (final IOException e) {
        }

        return dir.toURI().toURL();
    }

    private static URL resource(final String name) {
        return FileEntryIteratorTest.class.getResource(name);
    }
}
