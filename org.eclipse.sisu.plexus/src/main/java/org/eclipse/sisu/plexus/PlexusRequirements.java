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
package org.eclipse.sisu.plexus;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Provider;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.EntryMapAdapter;
import org.eclipse.sisu.wire.EntrySetAdapter;

/**
 * Creates {@link Provider}s for properties with @{@link Requirement} metadata.
 */
final class PlexusRequirements {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<PlexusBeanLocator> locatorProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirements(final TypeEncounter<?> encounter) {
        locatorProvider = encounter.getProvider(PlexusBeanLocator.class);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides Plexus components that match the given property requirement.
     *
     * @param requirement The Plexus requirement
     * @param property The bean property
     * @return Provider that provides required Plexus components for the given property
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Provider<T> lookup(final Requirement requirement, final BeanProperty<T> property) {
        try {
            // deduce lookup from metadata + property details
            final TypeLiteral<T> expectedType = property.getType();
            final TypeLiteral<T> roleType = (TypeLiteral<T>) Roles.roleType(requirement, expectedType);
            final Class<?> rawType = expectedType.getRawType();

            final String[] hints = Hints.canonicalHints(requirement);

            if (Map.class == rawType) {
                return new RequirementMapProvider(locatorProvider, roleType, hints);
            } else if (List.class == rawType || Collection.class == rawType || Iterable.class == rawType) {
                return new RequirementListProvider(locatorProvider, roleType, hints);
            } else if (Set.class == rawType) {
                return new RequirementSetProvider(locatorProvider, roleType, hints);
            }

            return new RequirementProvider(locatorProvider, roleType, hints);
        } catch (final RuntimeException e) {
            // defer until later...
            return new Provider<T>() {
                @Override
                public T get() {
                    throw new ProvisionException("Error in requirement: " + property, e);
                }
            };
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Abstract {@link Provider} that locates Plexus beans on-demand.
     */
    private abstract static class AbstractRequirementProvider<S, T> implements Provider<S> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Provider<PlexusBeanLocator> locatorProvider;

        final TypeLiteral<T> type;

        private final String[] hints;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        AbstractRequirementProvider(
                final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type, final String[] hints) {
            this.locatorProvider = locatorProvider;

            this.type = type;
            this.hints = hints;
        }

        // ----------------------------------------------------------------------
        // Locally-shared methods
        // ----------------------------------------------------------------------

        final Iterable<? extends Entry<String, T>> locate() {
            return locatorProvider.get().locate(type, hints);
        }
    }

    /**
     * {@link Provider} of Plexus requirement maps.
     */
    private static final class RequirementMapProvider<T> extends AbstractRequirementProvider<Map<String, T>, T> {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementMapProvider(
                final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type, final String[] hints) {
            super(locatorProvider, type, hints);
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Map<String, T> get() {
            return new EntryMapAdapter<>(locate());
        }
    }

    /**
     * {@link Provider} of Plexus requirement lists.
     */
    private static final class RequirementListProvider<T> extends AbstractRequirementProvider<List<T>, T> {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementListProvider(
                final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type, final String[] hints) {
            super(locatorProvider, type, hints);
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public List<T> get() {
            return new EntryListAdapter<>(locate());
        }
    }

    /**
     * {@link Provider} of Plexus requirement sets.
     */
    private static final class RequirementSetProvider<T> extends AbstractRequirementProvider<Set<T>, T> {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementSetProvider(
                final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type, final String[] hints) {
            super(locatorProvider, type, hints);
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Set<T> get() {
            return new EntrySetAdapter<>(locate());
        }
    }

    /**
     * {@link Provider} of a single Plexus requirement.
     */
    private static final class RequirementProvider<T> extends AbstractRequirementProvider<T, T> {
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        RequirementProvider(
                final Provider<PlexusBeanLocator> locatorProvider, final TypeLiteral<T> type, final String[] hints) {
            super(locatorProvider, type, hints);
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public T get() {
            // pick first bean: supports both specific and wildcard lookup
            final Iterator<? extends Entry<String, T>> i = locate().iterator();
            if (i.hasNext()) {
                return i.next().getValue();
            }
            return Roles.throwMissingComponentException(type, null);
        }
    }
}
