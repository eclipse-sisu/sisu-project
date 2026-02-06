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
package org.eclipse.sisu.bean;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link Module} that provides lifecycle management by following {@link org.eclipse.sisu.PostConstruct}
 * and {@link org.eclipse.sisu.PreDestroy} annotations, or corresponding JSR250 {@link javax.annotation.PostConstruct}
 * and {@link javax.annotation.PreDestroy} annotations. The lifecycle can be controlled with the associated
 * {@link BeanManager}.
 */
public final class LifecycleModule implements Module {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /* These classes map the Guice SPI to the BeanManager SPI */

    private final Matcher<TypeLiteral<?>> matcher = new AbstractMatcher<TypeLiteral<?>>() {
        @Override
        public boolean matches(final TypeLiteral<?> type) {
            return manager.manage(type.getRawType());
        }
    };

    private final TypeListener typeListener = new TypeListener() {
        private final InjectionListener<Object> listener = new InjectionListener<Object>() {
            @Override
            public void afterInjection(final Object bean) {
                manager.manage(bean);
            }
        };

        @Override
        public <B> void hear(final TypeLiteral<B> type, final TypeEncounter<B> encounter) {
            encounter.register(listener);
        }
    };

    final BeanManager manager;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public LifecycleModule() {
        this(new LifecycleManager());
    }

    public LifecycleModule(final BeanManager manager) {
        this.manager = manager;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void configure(final Binder binder) {
        binder.bind(BeanManager.class).toInstance(manager);
        binder.bindListener(matcher, typeListener);
    }
}
