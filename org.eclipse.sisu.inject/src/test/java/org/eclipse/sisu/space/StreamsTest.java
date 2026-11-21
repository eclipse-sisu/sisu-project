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
package org.eclipse.sisu.space;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@BaseTests
class StreamsTest {
    @Test
    void openReadsRewrittenJar(@TempDir Path tempDir) throws Exception {
        final Path jar = tempDir.resolve("resources.jar");
        writeJar(jar, "first");
        assertEquals("first", readEntry(jar));

        // Rewritten in place, like a plugin jar rebuilt in the reactor.
        writeJar(jar, "second-and-longer-than-the-first");
        assertEquals("second-and-longer-than-the-first", readEntry(jar));

        // No cached JarFile keeps the jar open (fails on Windows while it is cached).
        Files.delete(jar);
    }

    private static String readEntry(final Path jar) throws IOException {
        final URL url = new URL("jar:" + jar.toUri().toURL() + "!/entry.txt");
        try (InputStream in = Streams.open(url)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.readLine();
            }
        }
    }

    private static void writeJar(final Path jar, final String content) throws IOException {
        try (OutputStream out = Files.newOutputStream(jar);
                JarOutputStream jarOut = new JarOutputStream(out)) {
            jarOut.putNextEntry(new ZipEntry("entry.txt"));
            jarOut.write((content + "\n").getBytes(StandardCharsets.UTF_8));
            jarOut.closeEntry();
        }
    }
}
