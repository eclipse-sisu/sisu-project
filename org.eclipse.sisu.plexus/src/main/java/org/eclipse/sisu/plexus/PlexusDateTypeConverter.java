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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * {@link TypeConverter} {@link Module} that converts Plexus formatted date strings into {@link Date}s.
 */
public final class PlexusDateTypeConverter extends AbstractMatcher<TypeLiteral<?>> implements TypeConverter, Module {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final DateFormat[] PLEXUS_DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S a", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    };

    private static final String CONVERSION_ERROR = "Cannot convert: \"%s\" to: %s";

    static {
        for (final DateFormat f : PLEXUS_DATE_FORMATS) {
            f.setLenient(false); // turn off lenient parsing as it gives odd results
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void configure(final Binder binder) {
        // we're both matcher and converter
        binder.convertToTypes(this, this);
    }

    @Override
    public boolean matches(final TypeLiteral<?> type) {
        return Date.class == type.getRawType();
    }

    @Override
    public Object convert(final String value, final TypeLiteral<?> toType) {
        for (final DateFormat f : PLEXUS_DATE_FORMATS) {
            try {
                synchronized (f) // formats are not thread-safe!
                {
                    return f.parse(value);
                }
            } catch (final ParseException e) {
                continue; // try another format
            }
        }
        throw new IllegalArgumentException(String.format(CONVERSION_ERROR, value, Date.class));
    }
}
