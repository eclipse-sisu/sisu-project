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

import java.lang.annotation.Annotation;
import org.codehaus.plexus.component.annotations.Configuration;

/**
 * Runtime implementation of Plexus @{@link Configuration} annotation.
 */
public final class ConfigurationImpl implements Configuration {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String name;

    private final String value;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ConfigurationImpl(final String name, final String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("@Configuration cannot contain null values");
        }

        this.name = name;
        this.value = value;
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }

        if (rhs instanceof Configuration) {
            final Configuration conf = (Configuration) rhs;

            return name.equals(conf.name()) && value.equals(conf.value());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (127 * "name".hashCode() ^ name.hashCode()) + (127 * "value".hashCode() ^ value.hashCode());
    }

    @Override
    public String toString() {
        return String.format("@%s(name=%s, value=%s)", Configuration.class.getName(), name, value);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Configuration.class;
    }
}
