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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.util.Iterator;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

class PriorityTest {
    static interface Bean {}

    // base priority of default implementation is 0
    static class DefaultBean implements Bean {}

    @org.eclipse.sisu.Priority(1000)
    static class MediumPriorityBean implements Bean {}

    @javax.annotation.Priority(3000)
    static class HighPriorityBean implements Bean {}

    // base priority of non-default (alternative) implementation is Integer.MIN_VALUE
    static class AlternativeBean implements Bean {}

    @javax.annotation.Priority(-5000)
    static class LowPriorityProvider implements javax.inject.Provider<Bean> {
        @Override
        public Bean get() {
            return new DefaultBean();
        }
    }

    @javax.annotation.Priority(5000)
    static class HighPriorityProvider implements javax.inject.Provider<Bean> {
        @Override
        public Bean get() {
            return new DefaultBean();
        }
    }

    @javax.annotation.Priority(-5000)
    static class LowPriorityGuiceProvider implements Provider<Bean> {
        @Override
        public Bean get() {
            return new DefaultBean();
        }
    }

    @javax.annotation.Priority(5000)
    static class HighPriorityGuiceProvider implements Provider<Bean> {
        @Override
        public Bean get() {
            return new DefaultBean();
        }
    }

    static Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(Bean.class).annotatedWith(Names.named("ALT")).to(AlternativeBean.class);
            bind(Bean.class).annotatedWith(Names.named("HI")).to(HighPriorityBean.class);
            bind(Bean.class).to(DefaultBean.class);
            bind(Bean.class).annotatedWith(Names.named("MED")).to(MediumPriorityBean.class);
            binder().withSource(Sources.prioritize(2000))
                    .bind(Bean.class)
                    .annotatedWith(Names.named("SRC"))
                    .to(DefaultBean.class);
            LinkedBindingBuilder<Bean> hi = bind(Bean.class).annotatedWith(Names.named("(HI)"));
            LinkedBindingBuilder<Bean> lo = bind(Bean.class).annotatedWith(Names.named("(LO)"));
            try {
                hi.toProvider(new HighPriorityProvider());
                lo.toProvider(LowPriorityProvider.class);
            } catch (NoSuchMethodError e) // Guice3 doesn't let you bind javax.inject.Providers
            {
                hi.toProvider(new HighPriorityGuiceProvider());
                lo.toProvider(new LowPriorityGuiceProvider());
            }
        }
    });

    @Test
    void testPriorityOverride() {
        final BeanLocator locator = injector.getInstance(BeanLocator.class);

        Iterator<? extends Entry<Named, Bean>> i;

        i = locator.<Named, Bean>locate(Key.get(Bean.class, Named.class)).iterator();

        assertTrue(i.hasNext());
        assertEquals(Names.named("(HI)"), i.next().getKey());
        assertEquals(Names.named("HI"), i.next().getKey());
        assertEquals(Names.named("SRC"), i.next().getKey());
        assertEquals(Names.named("MED"), i.next().getKey());
        assertEquals(Names.named("default"), i.next().getKey());
        assertEquals(Names.named("(LO)"), i.next().getKey());
        assertEquals(Names.named("ALT"), i.next().getKey());
        assertFalse(i.hasNext());
    }
}
