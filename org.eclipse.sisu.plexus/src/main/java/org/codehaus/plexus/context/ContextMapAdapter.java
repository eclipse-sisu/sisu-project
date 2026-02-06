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
package org.codehaus.plexus.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ContextMapAdapter implements Map<Object, Object> {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Map<Object, Object> contextData;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ContextMapAdapter(final Context context) {
        contextData = context.getContextData();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /*
     * Only method used when interpolating Plexus configuration
     */
    @Override
    public Object get(final Object key) {
        final Object value = contextData.get(key);
        return value instanceof String ? value : null;
    }

    // ----------------------------------------------------------------------
    // Unsupported methods
    // ----------------------------------------------------------------------

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object put(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Object> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
