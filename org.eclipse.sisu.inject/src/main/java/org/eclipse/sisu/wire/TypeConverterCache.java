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

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

/**
 * Lazy cache of known {@link TypeConverter}s.
 */
@Singleton
final class TypeConverterCache {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<TypeLiteral<?>, TypeConverter> converterMap = new ConcurrentHashMap<>(16, 0.75f, 1);

    private final Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    TypeConverterCache(final Injector injector) {
        this.injector = injector;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeConverter getTypeConverter(final TypeLiteral<?> type) {
        TypeConverter converter = converterMap.get(type);
        if (null == converter) {
            for (final TypeConverterBinding b : injector.getTypeConverterBindings()) {
                if (b.getTypeMatcher().matches(type)) {
                    converter = b.getTypeConverter();
                    converterMap.put(type, converter);
                    break;
                }
            }
        }
        return converter;
    }
}
