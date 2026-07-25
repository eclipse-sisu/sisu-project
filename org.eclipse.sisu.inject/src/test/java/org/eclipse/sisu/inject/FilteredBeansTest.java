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
package org.eclipse.sisu.inject;

import static org.junit.jupiter.api.Assertions.*;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import javax.inject.Named;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.RankedBindingsTest.Bean;
import org.eclipse.sisu.inject.RankedBindingsTest.BeanImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilteredBeansTest {

    Injector injector;

    @BeforeEach
    void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Bean.class).annotatedWith(Names.named("Marked1")).to(BeanImpl.class);
                bind(Bean.class).annotatedWith(Names.named("Marked2")).to(BeanImpl.class);
                bind(Bean.class).annotatedWith(Names.named("Marked3")).to(BeanImpl.class);
                bind(Bean.class).annotatedWith(Names.named("Marked4")).to(BeanImpl.class);
            }
        });
    }

    @Test
    void withoutSupplier() {
        final FilteredBeans<Named, Bean> beans = new FilteredBeans<>(null, locate(Key.get(Bean.class)));

        AtomicInteger count = new AtomicInteger(0);
        beans.iterator().forEachRemaining(b -> count.incrementAndGet());
        assertEquals(4, count.get());
    }

    @Test
    void withNullSupplier() {
        final FilteredBeans<Named, Bean> beans = new FilteredBeans<>(() -> null, locate(Key.get(Bean.class)));

        AtomicInteger count = new AtomicInteger(0);
        beans.iterator().forEachRemaining(b -> count.incrementAndGet());
        assertEquals(4, count.get());
    }

    @Test
    void withTrueSupplier() {
        final FilteredBeans<Named, Bean> beans = new FilteredBeans<>(() -> b -> true, locate(Key.get(Bean.class)));

        AtomicInteger count = new AtomicInteger(0);
        beans.iterator().forEachRemaining(b -> count.incrementAndGet());
        assertEquals(4, count.get());
    }

    @Test
    void withFalseSupplier() {
        final FilteredBeans<Named, Bean> beans = new FilteredBeans<>(() -> b -> false, locate(Key.get(Bean.class)));

        AtomicInteger count = new AtomicInteger(0);
        beans.iterator().forEachRemaining(b -> count.incrementAndGet());
        assertEquals(0, count.get());
    }

    @Test
    void withFilteringSupplier() {
        final Set<String> allowedBeans = new HashSet<>(Arrays.asList("Marked1", "Marked3"));
        final Predicate<BeanEntry<Named, Bean>> predicate =
                b -> allowedBeans.contains(b.getKey().value());
        final FilteredBeans<Named, Bean> beans = new FilteredBeans<>(() -> predicate, locate(Key.get(Bean.class)));

        AtomicInteger count = new AtomicInteger(0);
        beans.iterator().forEachRemaining(b -> {
            assertTrue(allowedBeans.contains(b.getKey().value()));
            count.incrementAndGet();
        });
        assertEquals(2, count.get());
    }

    private <T> LocatedBeans<Named, T> locate(final Key<T> key) {
        final RankedBindings<T> bindings = new RankedBindings<>(key.getTypeLiteral(), null);
        for (final Binding<T> b : injector.findBindingsByType(key.getTypeLiteral())) {
            bindings.add(b, 0);
        }
        return new LocatedBeans<>(key, bindings, null);
    }
}
