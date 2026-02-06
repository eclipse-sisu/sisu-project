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
package org.eclipse.sisu.space;

import java.util.Locale;
import java.util.Map;

/**
 * Common techniques for discovering bean implementations.
 *
 * @see org.eclipse.sisu.space.SpaceModule
 */
public enum BeanScanning {
    /**
     * Always scan
     */
    ON,

    /**
     * Never scan
     */
    OFF,

    /**
     * Scan once and cache results
     */
    CACHE,

    /**
     * Use local index (plug-ins)
     */
    INDEX,

    /**
     * Use global index (application)
     */
    GLOBAL_INDEX;

    /**
     * Selects the property named <i>"org.eclipse.sisu.space.BeanScanning"</i> and attempts to map its value to a
     * {@link BeanScanning} literal, ignoring case. Defaults to {@link BeanScanning#ON} for blank or null values.
     *
     * @param properties The properties
     * @return Selected scanning option
     * @throws IllegalArgumentException if the property value cannot be mapped
     */
    public static BeanScanning select(final Map<?, ?> properties) throws IllegalArgumentException {
        final String option = (String) properties.get(BeanScanning.class.getName());
        if (null == option || option.trim().length() == 0) {
            return BeanScanning.ON;
        }
        return Enum.valueOf(BeanScanning.class, option.toUpperCase(Locale.ENGLISH));
    }
}
