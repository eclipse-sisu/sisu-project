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
package org.eclipse.sisu.inject;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ProviderKeyBinding;
import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Qualifier;

/**
 * Enumerates the different strategies for qualifying {@link Binding}s against requirement {@link Key}s.
 */
enum QualifyingStrategy {
    // ----------------------------------------------------------------------
    // Enumerated values
    // ----------------------------------------------------------------------

    UNRESTRICTED {
        @Override
        final Annotation qualifies(final Key<?> requirement, final Binding<?> binding) {
            final Annotation qualifier = qualify(binding.getKey());
            return null != qualifier ? qualifier : BLANK_QUALIFIER;
        }
    },
    NAMED {
        @Override
        final Annotation qualifies(final Key<?> requirement, final Binding<?> binding) {
            final Annotation qualifier = qualify(binding.getKey());
            return qualifier instanceof Named ? qualifier : null;
        }
    },
    NAMED_WITH_ATTRIBUTES {
        @Override
        final Annotation qualifies(final Key<?> requirement, final Binding<?> binding) {
            final Annotation qualifier = qualify(binding.getKey());
            if (requirement.getAnnotation().equals(qualifier)) {
                return qualifier;
            }

            // special case for untargeted constructor binding: treat @Named on implementation as an alias
            if (binding instanceof ConstructorBinding<?>
                    && null == binding.getKey().getAnnotationType()) {
                final Class<?> clazz = binding.getKey().getTypeLiteral().getRawType();
                final javax.inject.Named alias = clazz.getAnnotation(javax.inject.Named.class);
                if (null != alias
                        && alias.value().equals(((Named) requirement.getAnnotation()).value())
                        && clazz.equals(Implementations.find(binding))) {
                    return requirement.getAnnotation();
                }
            }

            return null;
        }
    },
    MARKED {
        @Override
        final Annotation qualifies(final Key<?> requirement, final Binding<?> binding) {
            final Class<? extends Annotation> markerType = requirement.getAnnotationType();

            final Annotation qualifier = qualify(binding.getKey());
            if (markerType.isInstance(qualifier)) {
                return qualifier;
            }

            // binding only has marker type; upgrade to pseudo-instance
            if (markerType.equals(binding.getKey().getAnnotationType())
                    && markerType.getDeclaredMethods().length == 0) {
                // this stub is all we need for internal processing
                return new Annotation() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return markerType;
                    }
                };
            }

            if (binding instanceof ProviderKeyBinding<?>) {
                final Key<?> providerKey = ((ProviderKeyBinding<?>) binding).getProviderKey();
                return providerKey.getTypeLiteral().getRawType().getAnnotation(markerType);
            }

            final Class<?> implementation = Implementations.find(binding);
            return null != implementation ? implementation.getAnnotation(markerType) : null;
        }
    },
    MARKED_WITH_ATTRIBUTES {
        @Override
        final Annotation qualifies(final Key<?> requirement, final Binding<?> binding) {
            final Annotation qualifier = MARKED.qualifies(requirement, binding);
            return requirement.getAnnotation().equals(qualifier) ? qualifier : null;
        }
    };

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Annotation DEFAULT_QUALIFIER = Names.named("default");

    static final Annotation BLANK_QUALIFIER = Names.named("");

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to qualify the given {@link Binding} against the requirement {@link Key}.
     *
     * @param requirement The requirement key
     * @param binding The binding to qualify
     * @return Qualifier annotation when the binding qualifies; otherwise {@code null}
     */
    abstract Annotation qualifies(final Key<?> requirement, final Binding<?> binding);

    /**
     * Selects the appropriate qualifying strategy for the given requirement {@link Key}.
     *
     * @param key The requirement key
     * @return Qualifying strategy
     */
    static final QualifyingStrategy selectFor(final Key<?> key) {
        final Class<?> qualifierType = key.getAnnotationType();
        if (null == qualifierType) {
            return QualifyingStrategy.UNRESTRICTED;
        }
        if (Named.class == qualifierType) {
            return key.hasAttributes() ? QualifyingStrategy.NAMED_WITH_ATTRIBUTES : QualifyingStrategy.NAMED;
        }
        return key.hasAttributes() ? QualifyingStrategy.MARKED_WITH_ATTRIBUTES : QualifyingStrategy.MARKED;
    }

    /**
     * Computes a canonical {@link Qualifier} annotation for the given binding {@link Key}.
     *
     * @param key The key to qualify
     * @return Qualifier for the key
     */
    static final Annotation qualify(final Key<?> key) {
        if (null == key.getAnnotationType()) {
            return DEFAULT_QUALIFIER;
        }
        final Annotation qualifier = key.getAnnotation();
        if (qualifier instanceof Provider<?>) {
            // the qualifier is actually a wrapper around the original
            final Object original = ((Provider<?>) qualifier).get();
            return original instanceof Annotation ? (Annotation) original : DEFAULT_QUALIFIER;
        }
        return qualifier;
    }
}
