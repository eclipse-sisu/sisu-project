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

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import org.junit.jupiter.api.Test;

class DependencyVerifierTest {
    interface Greeter {}

    static class SimpleGreeter implements Greeter {}

    @Test
    void testConcreteUntargetedBindingVerifies() {
        final DependencyVerifier verifier = new DependencyVerifier();
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimpleGreeter.class);
            }
        };

        for (final Element element : Elements.getElements(module)) {
            if (element instanceof Binding) {
                final Boolean result = ((Binding<?>) element).acceptTargetVisitor(verifier);
                assertEquals(Boolean.TRUE, result);
            }
        }
    }

    @Test
    void testLinkedKeyBindingWithoutAnnotation() {
        final DependencyVerifier verifier = new DependencyVerifier();
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Greeter.class).to(SimpleGreeter.class);
            }
        };

        for (final Element element : Elements.getElements(module)) {
            if (element instanceof Binding) {
                final Boolean result = ((Binding<?>) element).acceptTargetVisitor(verifier);
                assertEquals(Boolean.TRUE, result);
            }
        }
    }

    @Test
    void testLinkedKeyBindingWithAnnotation() {
        final DependencyVerifier verifier = new DependencyVerifier();
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Greeter.class).to(com.google.inject.Key.get(SimpleGreeter.class, Names.named("x")));
            }
        };

        for (final Element element : Elements.getElements(module)) {
            if (element instanceof Binding) {
                final Boolean result = ((Binding<?>) element).acceptTargetVisitor(verifier);
                assertEquals(Boolean.TRUE, result);
            }
        }
    }

    @Test
    void testVisitOtherBinding() {
        final DependencyVerifier verifier = new DependencyVerifier();
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("hello");
            }
        };

        for (final Element element : Elements.getElements(module)) {
            if (element instanceof Binding) {
                final Boolean result = ((Binding<?>) element).acceptTargetVisitor(verifier);
                assertEquals(Boolean.TRUE, result);
            }
        }
    }

    @Test
    void testAbstractTypeIsAccepted() {
        final DependencyVerifier verifier = new DependencyVerifier();
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Greeter.class).to(Greeter.class);
            }
        };

        for (final Element element : Elements.getElements(module)) {
            if (element instanceof Binding) {
                final Boolean result = ((Binding<?>) element).acceptTargetVisitor(verifier);
                // abstract type won't be scanned (isConcrete returns false)
                assertEquals(Boolean.TRUE, result);
            }
        }
    }
}
