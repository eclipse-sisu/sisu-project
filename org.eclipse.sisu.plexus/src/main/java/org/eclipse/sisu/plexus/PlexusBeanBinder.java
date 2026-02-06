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
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import java.util.List;
import org.eclipse.sisu.bean.BeanBinder;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.PropertyBinder;

/**
 * {@link BeanBinder} that binds bean properties according to Plexus metadata.
 */
final class PlexusBeanBinder implements BeanBinder, InjectionListener<Object> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanManager manager;

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusBeanBinder(final BeanManager manager, final List<PlexusBeanSource> sources) {
        this.manager = manager;
        this.sources = sources.toArray(new PlexusBeanSource[sources.size()]);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public <B> PropertyBinder bindBean(final TypeLiteral<B> type, final TypeEncounter<B> encounter) {
        final Class<?> clazz = type.getRawType();
        if (null != manager && manager.manage(clazz)) {
            encounter.register(this);
        }
        for (final PlexusBeanSource source : sources) {
            // use first source that has metadata for the given implementation
            final PlexusBeanMetadata metadata = source.getBeanMetadata(clazz);
            if (metadata != null) {
                return new PlexusPropertyBinder(manager, encounter, metadata);
            }
        }
        return null; // no need to auto-bind
    }

    @Override
    public void afterInjection(final Object bean) {
        manager.manage(bean);
    }
}
