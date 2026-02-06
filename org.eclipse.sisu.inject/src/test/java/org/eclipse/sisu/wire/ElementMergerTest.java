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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;

class ElementMergerTest {
    interface Service {}

    static class ServiceImpl implements Service {}

    @Test
    void testDuplicateBindingsAreMerged() {
        final Module moduleA = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Service.class).to(ServiceImpl.class);
            }
        };
        final Module moduleB = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Service.class).to(ServiceImpl.class);
            }
        };

        // MergedModule uses ElementMerger internally to deduplicate
        final Module merged = new MergedModule(moduleA, moduleB);
        final Injector injector = Guice.createInjector(merged);
        assertNotNull(injector.getInstance(Service.class));
    }

    @Test
    void testVisitOtherPassThrough() {
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("hello");
                requireBinding(Key.get(String.class));
            }
        };

        final Module merged = new MergedModule(module);
        final Injector injector = Guice.createInjector(merged);
        assertNotNull(injector.getInstance(String.class));
    }

    @Test
    void testMultipleModulesMerged() {
        final Module moduleA = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("m1")).toInstance("one");
            }
        };
        final Module moduleB = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("m2")).toInstance("two");
            }
        };
        final Module moduleC = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("m3")).toInstance("three");
            }
        };

        final Module merged = new MergedModule(moduleA, moduleB, moduleC);
        final Injector injector = Guice.createInjector(merged);
        assertNotNull(injector.getInstance(Key.get(String.class, Names.named("m1"))));
        assertNotNull(injector.getInstance(Key.get(String.class, Names.named("m2"))));
        assertNotNull(injector.getInstance(Key.get(String.class, Names.named("m3"))));
    }
}
