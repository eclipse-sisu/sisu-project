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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class LogsConsoleSinkTest {
    @Test
    void testConsoleSinkTraceEnabled() {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        assertTrue(consoleSink.isTraceEnabled());
        assertTrue(consoleSink.isDebugEnabled());
    }

    @Test
    void testConsoleSinkTrace() throws IOException {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        final PrintStream originalOut = System.out;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            consoleSink.trace("test trace message", null);
            final String output = outputStream.toString();
            assertTrue(output.contains("TRACE"));
            assertTrue(output.contains("test trace message"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testConsoleSinkTraceWithCause() throws IOException {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        final PrintStream originalOut = System.out;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            consoleSink.trace("trace with cause", new RuntimeException("test error"));
            final String output = outputStream.toString();
            assertTrue(output.contains("trace with cause"));
            assertTrue(output.contains("test error"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testConsoleSinkDebug() throws IOException {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        final PrintStream originalOut = System.out;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            consoleSink.debug("test debug message", null);
            final String output = outputStream.toString();
            assertTrue(output.contains("DEBUG"));
            assertTrue(output.contains("test debug message"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testConsoleSinkWarn() throws IOException {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        final PrintStream originalErr = System.err;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setErr(new PrintStream(outputStream));
            consoleSink.warn("test warn message", null);
            final String output = outputStream.toString();
            assertTrue(output.contains("WARN"));
            assertTrue(output.contains("test warn message"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testConsoleSinkWarnWithCause() throws IOException {
        final Logs.ConsoleSink consoleSink = new Logs.ConsoleSink();
        final PrintStream originalErr = System.err;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setErr(new PrintStream(outputStream));
            consoleSink.warn("warn with cause", new RuntimeException("warn error"));
            final String output = outputStream.toString();
            assertTrue(output.contains("warn with cause"));
            assertTrue(output.contains("warn error"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testIdentityToString() {
        assertNull(Logs.identityToString(null));
        final String identity = Logs.identityToString("hello");
        assertNotNull(identity);
        assertTrue(identity.startsWith("java.lang.String@"));
    }

    @Test
    void testToStringModule() {
        final String moduleString = Logs.toString(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("test");
            }
        });
        assertNotNull(moduleString);
        assertTrue(moduleString.contains("elements"));
    }

    @Test
    void testToStringInjector() {
        final String injectorString = Logs.toString(Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("test");
            }
        }));
        assertNotNull(injectorString);
        assertTrue(injectorString.contains("explicit bindings"));
        assertTrue(injectorString.contains("implicit bindings"));
    }
}
