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
package org.eclipse.sisu.launch;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;

/**
 * Bootstrap class that creates a static {@link Injector} by scanning the current class-path for beans.
 */
public final class Main implements Module {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> properties;

    private final String[] args;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Main(final Map<?, ?> properties, final String... args) {
        this.properties = Collections.unmodifiableMap(properties);
        this.args = args;
    }

    // ----------------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------------

    public static void main(final String... args) {
        boot(System.getProperties(), args);
    }

    public static <T> T boot(final Class<T> type, final String... args) {
        return boot(System.getProperties(), args).getInstance(type);
    }

    public static <T> T boot(final Class<T> type, final String[] args, final Module... bindings) {
        return boot(System.getProperties(), args, bindings).getInstance(type);
    }

    public static Injector boot(final Map<?, ?> properties, final String... args) {
        return boot(properties, args, new Module[0]);
    }

    public static Injector boot(final Map<?, ?> properties, final String[] args, final Module... bindings) {
        final Module[] modules = new Module[bindings.length + 1];
        System.arraycopy(bindings, 0, modules, 1, bindings.length);
        modules[0] = new Main(properties, args);

        final BeanScanning scanning = BeanScanning.select(properties);
        final Module app = wire(scanning, modules);
        final Injector injector = Guice.createInjector(app);

        return injector;
    }

    public static Module wire(final BeanScanning scanning, final Module... bindings) {
        final Module[] modules = new Module[bindings.length + 1];
        System.arraycopy(bindings, 0, modules, 0, bindings.length);

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        modules[bindings.length] = new SpaceModule(new URLClassSpace(tccl), scanning);

        return new WireModule(modules);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void configure(final Binder binder) {
        binder.bind(ParameterKeys.PROPERTIES).toInstance(properties);
        binder.bind(ShutdownThread.class).asEagerSingleton();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Provides
    @Parameters
    String[] parameters() {
        return args.clone();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class ShutdownThread extends Thread {
        private final MutableBeanLocator locator;

        @Inject
        ShutdownThread(final MutableBeanLocator locator) {
            this.locator = locator;

            Runtime.getRuntime().addShutdownHook(this);
        }

        @Override
        public void run() {
            locator.clear();
        }
    }
}
