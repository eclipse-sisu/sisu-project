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
package org.eclipse.sisu.wire;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import java.util.Arrays;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.wire.WireModule.Strategy;

/**
 * Child {@link WireModule} that avoids wiring dependencies that already exist in a parent {@link Injector}.
 */
public final class ChildWireModule implements Module {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Injector parent;

    private final Iterable<Module> modules;

    private Strategy strategy = Strategy.DEFAULT;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ChildWireModule(final Injector parent, final Module... modules) {
        this(parent, Arrays.asList(modules));
    }

    public ChildWireModule(final Injector parent, final Iterable<Module> modules) {
        this.modules = modules;
        this.parent = parent;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Applies a new wiring {@link Strategy} to the current module.
     *
     * @param _strategy The new strategy
     * @return Updated module
     */
    public Module with(final Strategy _strategy) {
        strategy = _strategy;
        return this;
    }

    @Override
    public void configure(final Binder binder) {
        // workaround to support 'autoPublish' in child injectors
        binder.requestStaticInjection(DefaultBeanLocator.class);

        // ignore any inherited bindings/dependencies
        final ElementAnalyzer analyzer = new ElementAnalyzer(binder);
        for (Injector i = parent; i != null; i = i.getParent()) {
            analyzer.ignoreKeys(i.getAllBindings().keySet());
        }

        // rest of this is the same as WireModule.configure...
        for (final Element e : Elements.getElements(modules)) {
            e.acceptVisitor(analyzer);
        }
        analyzer.apply(strategy);
    }
}
