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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.EventListener;
import java.util.RandomAccess;
import java.util.concurrent.Callable;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.BeanLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QualifiedTypesTest {
    @Named
    static class DefaultB01 {}

    @Named("default")
    static class B01 extends Thread {}

    @Named
    @Typed
    static class DefaultB02 implements RandomAccess, EventListener {}

    @Named
    @Typed(EventListener.class)
    static class B02 implements RandomAccess, EventListener {}

    @Named
    static class B03 implements EventListener {}

    @Named
    @EagerSingleton
    static class DefaultB03 {
        static boolean initialized;

        @Inject
        void initialize() {
            initialized = true;
        }
    }

    @Named("TEST")
    static class DefaultB04 {}

    @Named("TEST")
    static class B04 {}

    @Named
    static class B05EventListener implements Callable<String>, EventListener, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String call() {
            return "GO";
        }
    }

    @Named
    @Typed({EventListener.class, Callable.class})
    static class B06 implements Callable<String>, EventListener, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String call() {
            return "GO";
        }
    }

    abstract static class AbstractB01 {}

    @Typed({AbstractB02.class, EventListener.class})
    abstract static class AbstractB02 implements RandomAccess, EventListener {}

    abstract static class AbstractB03 implements EventListener {}

    @Named
    static class SubclassB00 extends B01 {}

    @Named
    static class SubclassB01 extends AbstractB01 {}

    @Named
    static class SubclassB02 extends AbstractB02 {}

    @Named
    static class SubclassB03EventListener extends AbstractB03 {}

    @Named("RENAME")
    static class SubclassB04EventListener extends AbstractB03 {}

    @Named
    @org.eclipse.sisu.Typed(B02.class)
    static class SubclassB06 extends B02 {}

    @Named
    @Typed(Serializable.class)
    static class SubclassB07 extends SubclassB06 implements Callable<String>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String call() {
            return "GO";
        }
    }

    @Named
    @Typed
    static class SubclassB08 extends B02 implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Legacy {}

    static class LegacyImpl implements Legacy {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Legacy.class;
        }
    }

    @Legacy
    static class LegacyCallable implements Callable<String> {
        @Override
        public String call() {
            return "GO";
        }
    }

    private BeanLocator locator;

    private Injector injector;

    @BeforeEach
    void setUp() throws Exception {
        final ClassSpace space = new URLClassSpace(
                getClass().getClassLoader(), new URL[] {getClass().getResource("")});
        injector = Guice.createInjector(new SpaceModule(space));
        locator = injector.getInstance(BeanLocator.class);

        assertTrue(DefaultB03.initialized);
    }

    private void checkDefaultBinding(final Class<?> api, final Class<?> imp) {
        final Annotation defaultName = Names.named("default");

        assertSame(imp, locator.locate(Key.get(api)).iterator().next().getImplementationClass());
        assertSame(
                imp, locator.locate(Key.get(api, Named.class)).iterator().next().getImplementationClass());
        assertSame(
                imp, locator.locate(Key.get(api, defaultName)).iterator().next().getImplementationClass());
    }

    private void checkNamedBinding(final Class<?> api, final String name, final Class<?> imp) {
        assertSame(
                imp,
                locator.locate(Key.get(api, Names.named(name)))
                        .iterator()
                        .next()
                        .getImplementationClass());
    }

    private void checkLegacyBinding(final Class<?> api, final Class<?> imp) {
        assertSame(
                imp,
                locator.locate(Key.get(api, Legacy.class)).iterator().next().getImplementationClass());
    }

    @Test
    void testQualifiedBindings() {
        checkDefaultBinding(DefaultB01.class, DefaultB01.class);
        checkDefaultBinding(DefaultB02.class, DefaultB02.class);
        checkDefaultBinding(DefaultB03.class, DefaultB03.class);
        checkDefaultBinding(DefaultB04.class, DefaultB04.class);

        checkDefaultBinding(Thread.class, B01.class);
        checkDefaultBinding(B04.class, B04.class);

        checkNamedBinding(EventListener.class, B02.class.getName(), B02.class);
        checkNamedBinding(EventListener.class, B03.class.getName(), B03.class);

        checkNamedBinding(EventListener.class, B05EventListener.class.getName(), B05EventListener.class);
        checkNamedBinding(EventListener.class, B06.class.getName(), B06.class);

        checkNamedBinding(B01.class, SubclassB00.class.getName(), SubclassB00.class);
        checkNamedBinding(AbstractB01.class, SubclassB01.class.getName(), SubclassB01.class);

        checkNamedBinding(AbstractB02.class, SubclassB02.class.getName(), SubclassB02.class);
        checkNamedBinding(
                EventListener.class, SubclassB03EventListener.class.getName(), SubclassB03EventListener.class);
        checkNamedBinding(EventListener.class, "RENAME", SubclassB04EventListener.class);
        checkNamedBinding(B02.class, SubclassB06.class.getName(), SubclassB06.class);

        checkNamedBinding(Serializable.class, SubclassB07.class.getName(), SubclassB07.class);
        checkNamedBinding(Serializable.class, SubclassB08.class.getName(), SubclassB08.class);

        checkLegacyBinding(Callable.class, LegacyCallable.class);
    }

    @ImplementedBy(AImpl.class)
    interface A {}

    static class AImpl implements A {}

    static class Ambiguous implements Serializable, EventListener {
        private static final long serialVersionUID = 1L;
    }

    @SuppressWarnings("rawtypes")
    static class RawMediator implements Mediator {
        @Override
        public void add(final BeanEntry bean, final Object watcher) throws Exception {}

        @Override
        public void remove(final BeanEntry bean, final Object watcher) throws Exception {}
    }

    abstract class AbstractNamedMediator implements Mediator<Named, Object, Object> {}

    static class BadBindings implements Module {
        @Override
        public void configure(final Binder binder) {
            final QualifiedTypeListener listener = new QualifiedTypeBinder(binder);

            listener.hear(Ambiguous.class, null);
            listener.hear(RawMediator.class, null);
            listener.hear(AbstractNamedMediator.class, null);
            listener.hear(AbstractModule.class, null);
        }
    }

    @Test
    void testBadBindings() {
        try {
            Guice.createInjector(new BadBindings());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
