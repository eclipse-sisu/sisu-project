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
package org.codehaus.plexus.component.configurator.converters.basic;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

public class ByteConverter extends AbstractBasicConverter {
    @Override
    public boolean canConvert(final Class<?> type) {
        return byte.class.equals(type) || Byte.class.equals(type);
    }

    @Override
    public Object fromString(final String value) throws ComponentConfigurationException {
        try {
            return Byte.decode(value);
        } catch (final NumberFormatException e) {
            throw new ComponentConfigurationException("Cannot convert '" + value + "' to byte", e);
        }
    }
}
