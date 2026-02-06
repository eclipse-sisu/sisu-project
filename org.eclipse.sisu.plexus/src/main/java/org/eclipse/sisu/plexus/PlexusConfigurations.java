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

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import javax.inject.Provider;
import org.codehaus.plexus.component.annotations.Configuration;
import org.eclipse.sisu.bean.BeanProperty;

/**
 * Creates {@link Provider}s for properties with @{@link Configuration} metadata.
 */
final class PlexusConfigurations {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<PlexusBeanConverter> converterProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusConfigurations(final TypeEncounter<?> encounter) {
        converterProvider = encounter.getProvider(PlexusBeanConverter.class);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides values that match the given property configuration.
     *
     * @param configuration The Plexus configuration
     * @param property The bean property
     * @return Provider that provides configured values for the given property
     */
    public <T> Provider<T> lookup(final Configuration configuration, final BeanProperty<T> property) {
        return new ConfigurationProvider<>(converterProvider, property.getType(), configuration.value());
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Provider} of Plexus configurations.
     */
    private static final class ConfigurationProvider<T> implements Provider<T> {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Provider<PlexusBeanConverter> converterProvider;

        private final TypeLiteral<T> type;

        private final String value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ConfigurationProvider(
                final Provider<PlexusBeanConverter> converterProvider, final TypeLiteral<T> type, final String value) {
            this.converterProvider = converterProvider;

            this.type = type;
            this.value = value;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public T get() {
            return converterProvider.get().convert(type, value);
        }
    }
}
