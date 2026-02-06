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

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link TypeListener} that listens for bean types and arranges for their properties to be injected.
 */
public final class BeanListener implements TypeListener {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanBinder beanBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanListener(final BeanBinder beanBinder) {
        this.beanBinder = beanBinder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public <B> void hear(final TypeLiteral<B> type, final TypeEncounter<B> encounter) {
        final PropertyBinder propertyBinder = beanBinder.bindBean(type, encounter);
        if (null == propertyBinder) {
            return; // no properties to bind
        }

        final List<PropertyBinding> bindings = new ArrayList<>();
        final Set<String> visited = new HashSet<>();

        for (final BeanProperty<?> property : new BeanProperties(type.getRawType())) {
            if (property.getAnnotation(javax.inject.Inject.class) != null
                    || property.getAnnotation(com.google.inject.Inject.class) != null) {
                continue; // these properties will have already been injected by Guice
            }
            final String name = property.getName();
            if (visited.add(name)) {
                try {
                    final PropertyBinding binding = propertyBinder.bindProperty(property);
                    if (binding == PropertyBinder.LAST_BINDING) {
                        break; // no more bindings
                    }
                    if (binding != null) {
                        bindings.add(binding);
                    } else {
                        visited.remove(name);
                    }
                } catch (final RuntimeException e) {
                    encounter.addError(new ProvisionException("Error binding: " + property, e));
                }
            }
        }

        if (!bindings.isEmpty()) {
            encounter.register(new BeanInjector<>(bindings));
        }
    }
}
