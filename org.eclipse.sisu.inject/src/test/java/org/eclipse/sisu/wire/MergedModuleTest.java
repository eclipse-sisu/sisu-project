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
package org.eclipse.sisu.wire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MergedModuleTest {
    @Test
    void testVarargsConstructor() {
        final Module moduleA = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("a")).toInstance("valueA");
            }
        };
        final Module moduleB = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("b")).toInstance("valueB");
            }
        };

        final Injector injector = Guice.createInjector(new MergedModule(moduleA, moduleB));
        assertEquals("valueA", injector.getInstance(com.google.inject.Key.get(String.class, Names.named("a"))));
        assertEquals("valueB", injector.getInstance(com.google.inject.Key.get(String.class, Names.named("b"))));
    }

    @Test
    void testIterableConstructor() {
        final Module moduleA = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("x")).toInstance("valueX");
            }
        };
        final Module moduleB = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("y")).toInstance("valueY");
            }
        };

        final Iterable<Module> modules = Arrays.asList(moduleA, moduleB);
        final Injector injector = Guice.createInjector(new MergedModule(modules));
        assertEquals("valueX", injector.getInstance(com.google.inject.Key.get(String.class, Names.named("x"))));
        assertEquals("valueY", injector.getInstance(com.google.inject.Key.get(String.class, Names.named("y"))));
    }

    @Test
    void testOverlappingBindingsAreMerged() {
        final Module moduleA = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("first");
            }
        };
        final Module moduleB = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("second");
            }
        };

        // First binding should win, second should be discarded
        final Injector injector = Guice.createInjector(new MergedModule(moduleA, moduleB));
        final String value = injector.getInstance(String.class);
        assertNotNull(value);
        assertEquals("first", value);
    }
}
