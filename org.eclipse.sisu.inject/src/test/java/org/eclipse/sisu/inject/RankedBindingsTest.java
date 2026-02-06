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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.sisu.Hidden;
import org.junit.jupiter.api.Test;

class RankedBindingsTest {
    @ImplementedBy(BeanImpl2.class)
    static interface Bean {}

    abstract static class AbstractBean implements Bean {}

    static class BeanImpl extends AbstractBean {}

    static class BeanImpl2 extends AbstractBean {}

    @Hidden
    static class InternalBeanImpl extends AbstractBean {}

    static Injector injector0 = Guice.createInjector();

    static Injector injector1 = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(Bean.class).annotatedWith(Names.named("1")).to(BeanImpl.class);
        }
    });

    static Injector injector2 = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(Bean.class).annotatedWith(Names.named("2")).to(BeanImpl.class);
            bind(Bean.class).to(BeanImpl.class);
        }
    });

    static Injector injector3 = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(Bean.class).annotatedWith(Names.named("3")).to(BeanImpl.class);
        }
    });

    @Test
    void testExistingPublishers() {
        final RankedSequence<BindingPublisher> publishers = new RankedSequence<>();

        RankingFunction function;

        try {
            new DefaultRankingFunction(-1);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        function = new DefaultRankingFunction(0);
        publishers.insert(new InjectorBindings(injector0, function), function.maxRank());
        function = new DefaultRankingFunction(1);
        publishers.insert(new InjectorBindings(injector1, function), function.maxRank());
        function = new DefaultRankingFunction(3);
        publishers.insert(new InjectorBindings(injector3, function), function.maxRank());
        function = new DefaultRankingFunction(2);
        publishers.insert(new InjectorBindings(injector2, function), function.maxRank());

        final RankedBindings<Bean> bindings = new RankedBindings<>(TypeLiteral.get(Bean.class), publishers);

        final Iterator<Binding<Bean>> itr = bindings.iterator();

        assertEquals(0, bindings.bindings.size());
        assertTrue(itr.hasNext());
        assertEquals(4, bindings.bindings.size());

        final Binding<Bean> explicitBinding = itr.next();
        assertNull(explicitBinding.getKey().getAnnotation());
        assertEquals(BeanImpl.class, Implementations.find(explicitBinding));

        assertEquals(4, bindings.bindings.size());
        assertTrue(itr.hasNext());
        assertEquals(4, bindings.bindings.size());

        assertEquals(Names.named("3"), itr.next().getKey().getAnnotation());
        assertTrue(itr.hasNext());
        assertEquals(Names.named("2"), itr.next().getKey().getAnnotation());
        assertTrue(itr.hasNext());
        assertEquals(Names.named("1"), itr.next().getKey().getAnnotation());

        assertFalse(itr.hasNext());
    }

    @Test
    void testPendingPublishers() {
        final RankedBindings<Bean> bindings = new RankedBindings<>(TypeLiteral.get(Bean.class), null);

        Iterator<Binding<Bean>> itr = bindings.iterator();

        assertFalse(itr.hasNext());

        try {
            itr.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            // expected
        }

        try {
            itr.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            // expected
        }

        RankingFunction function;

        assertEquals(0, bindings.bindings.size());
        function = new DefaultRankingFunction(2);
        bindings.add(new InjectorBindings(injector2, function), function.maxRank());
        assertEquals(0, bindings.bindings.size());

        assertTrue(itr.hasNext());

        assertEquals(2, bindings.bindings.size());
        Binding<Bean> explicitBinding = itr.next();
        assertNull(explicitBinding.getKey().getAnnotation());
        assertEquals(BeanImpl.class, Implementations.find(explicitBinding));
        assertEquals(2, bindings.bindings.size());

        assertTrue(itr.hasNext());

        assertEquals(2, bindings.bindings.size());
        function = new DefaultRankingFunction(0);
        bindings.add(new InjectorBindings(injector0, function), function.maxRank());
        assertEquals(2, bindings.bindings.size());
        function = new DefaultRankingFunction(3);
        bindings.add(new InjectorBindings(injector3, function), function.maxRank());
        assertEquals(2, bindings.bindings.size());

        assertTrue(itr.hasNext());

        assertEquals(3, bindings.bindings.size());

        assertEquals(3, bindings.bindings.size());
        function = new DefaultRankingFunction(1);
        bindings.add(new InjectorBindings(injector1, function), function.maxRank());
        assertEquals(3, bindings.bindings.size());

        assertTrue(itr.hasNext());

        assertEquals(4, bindings.bindings.size());
        assertEquals(Names.named("2"), itr.next().getKey().getAnnotation());
        assertEquals(4, bindings.bindings.size());

        assertTrue(itr.hasNext());

        assertEquals(4, bindings.bindings.size());

        itr = bindings.iterator();

        explicitBinding = itr.next();
        assertNull(explicitBinding.getKey().getAnnotation());
        assertEquals(BeanImpl.class, Implementations.find(explicitBinding));

        assertEquals(Names.named("3"), itr.next().getKey().getAnnotation());
        assertEquals(Names.named("2"), itr.next().getKey().getAnnotation());
        assertEquals(Names.named("1"), itr.next().getKey().getAnnotation());

        assertFalse(itr.hasNext());
    }

    @Test
    void testPublisherRemoval() {
        final BindingPublisher publisher0 = new InjectorBindings(injector0, new DefaultRankingFunction(0));
        final BindingPublisher publisher1 = new InjectorBindings(injector1, new DefaultRankingFunction(1));
        final BindingPublisher publisher2 = new InjectorBindings(injector2, new DefaultRankingFunction(2));
        final BindingPublisher publisher3 = new InjectorBindings(injector3, new DefaultRankingFunction(3));

        final RankedBindings<Bean> bindings = new RankedBindings<>(TypeLiteral.get(Bean.class), null);

        bindings.add(publisher0, 0);
        bindings.add(publisher1, 1);
        bindings.add(publisher2, 2);
        bindings.add(publisher3, 3);

        Iterator<Binding<Bean>> itr = bindings.iterator();

        bindings.remove(publisher1);
        assertTrue(itr.hasNext());

        Binding<Bean> explicitBinding = itr.next();
        assertNull(explicitBinding.getKey().getAnnotation());
        assertEquals(BeanImpl.class, Implementations.find(explicitBinding));

        bindings.remove(
                injector3.findBindingsByType(TypeLiteral.get(Bean.class)).get(0));
        bindings.remove(publisher2);
        bindings.remove(
                injector1.findBindingsByType(TypeLiteral.get(Bean.class)).get(0));

        assertFalse(itr.hasNext());

        itr = bindings.iterator();

        bindings.bindings.clear();

        bindings.add(publisher3, 0);
        bindings.add(publisher1, 0);
        bindings.add(publisher0, 0);
        bindings.add(publisher2, 0);

        assertTrue(itr.hasNext());

        explicitBinding = itr.next();
        assertNull(explicitBinding.getKey().getAnnotation());
        assertEquals(BeanImpl.class, Implementations.find(explicitBinding));

        assertTrue(itr.hasNext());
        assertEquals(Names.named("3"), itr.next().getKey().getAnnotation());
        assertTrue(itr.hasNext());
        assertEquals(Names.named("2"), itr.next().getKey().getAnnotation());
        assertTrue(itr.hasNext());
        assertEquals(Names.named("1"), itr.next().getKey().getAnnotation());

        assertFalse(itr.hasNext());
        assertFalse(itr.hasNext());
    }
}
