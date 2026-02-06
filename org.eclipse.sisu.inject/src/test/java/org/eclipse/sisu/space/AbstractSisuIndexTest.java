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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class AbstractSisuIndexTest {
    static class TestSisuIndex extends AbstractSisuIndex {
        final Map<String, String> inputData = new HashMap<>();

        final Map<String, StringWriter> outputData = new HashMap<>();

        final List<String> infoMessages = new ArrayList<>();

        final List<String> warnMessages = new ArrayList<>();

        @Override
        protected void info(final String message) {
            infoMessages.add(message);
        }

        @Override
        protected void warn(final String message) {
            warnMessages.add(message);
        }

        @Override
        protected Reader getReader(final String path) throws IOException {
            final String content = inputData.get(path);
            if (null != content) {
                return new StringReader(content);
            }
            throw new IOException("No such file: " + path);
        }

        @Override
        protected Writer getWriter(final String path) throws IOException {
            final StringWriter writer = new StringWriter();
            outputData.put(path, writer);
            return writer;
        }
    }

    @Test
    void testAddClassToIndex() {
        final TestSisuIndex index = new TestSisuIndex();
        index.addClassToIndex("javax.inject.Named", "com.example.Foo");
        index.addClassToIndex("javax.inject.Named", "com.example.Bar");
        index.flushIndex();

        final StringWriter writer = index.outputData.get("META-INF/sisu/javax.inject.Named");
        assertNotNull(writer);
        final String output = writer.toString();
        assertTrue(output.contains("com.example.Bar"));
        assertTrue(output.contains("com.example.Foo"));
    }

    @Test
    void testDuplicateClassesAreDeduped() {
        final TestSisuIndex index = new TestSisuIndex();
        index.addClassToIndex("javax.inject.Named", "com.example.Foo");
        index.addClassToIndex("javax.inject.Named", "com.example.Foo"); // duplicate
        index.flushIndex();

        final StringWriter writer = index.outputData.get("META-INF/sisu/javax.inject.Named");
        final String output = writer.toString();
        // count occurrences
        final int firstIndex = output.indexOf("com.example.Foo");
        final int secondIndex = output.indexOf("com.example.Foo", firstIndex + 1);
        assertEquals(-1, secondIndex); // should only appear once
    }

    @Test
    void testReadExistingTable() {
        final TestSisuIndex index = new TestSisuIndex();
        index.inputData.put("META-INF/sisu/javax.inject.Named", "com.example.Existing\n");

        index.addClassToIndex("javax.inject.Named", "com.example.New");
        index.flushIndex();

        final StringWriter writer = index.outputData.get("META-INF/sisu/javax.inject.Named");
        final String output = writer.toString();
        assertTrue(output.contains("com.example.Existing"));
        assertTrue(output.contains("com.example.New"));
    }

    @Test
    void testMultipleQualifiers() {
        final TestSisuIndex index = new TestSisuIndex();
        index.addClassToIndex("javax.inject.Named", "com.example.NamedClass");
        index.addClassToIndex("javax.inject.Qualifier", "com.example.QualifiedClass");
        index.flushIndex();

        assertTrue(index.outputData.containsKey("META-INF/sisu/javax.inject.Named"));
        assertTrue(index.outputData.containsKey("META-INF/sisu/javax.inject.Qualifier"));
    }

    @Test
    void testTablesSortedAlphabetically() {
        final TestSisuIndex index = new TestSisuIndex();
        index.addClassToIndex("javax.inject.Named", "com.example.Zebra");
        index.addClassToIndex("javax.inject.Named", "com.example.Alpha");
        index.addClassToIndex("javax.inject.Named", "com.example.Middle");
        index.flushIndex();

        final String output =
                index.outputData.get("META-INF/sisu/javax.inject.Named").toString();
        final int alphaPos = output.indexOf("Alpha");
        final int middlePos = output.indexOf("Middle");
        final int zebraPos = output.indexOf("Zebra");
        assertTrue(alphaPos < middlePos);
        assertTrue(middlePos < zebraPos);
    }
}
