/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ContextMapAdapter
    implements Map<Object, Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Map<Object, Object> contextData;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ContextMapAdapter( final Context context )
    {
        contextData = context.getContextData();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /*
     * Only method used when interpolating Plexus configuration
     */
    public Object get( final Object key )
    {
        final Object value = contextData.get( key );
        return value instanceof String ? value : null;
    }

    // ----------------------------------------------------------------------
    // Unsupported methods
    // ----------------------------------------------------------------------

    public int size()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey( final Object key )
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsValue( final Object value )
    {
        throw new UnsupportedOperationException();
    }

    public Object put( final Object key, final Object value )
    {
        throw new UnsupportedOperationException();
    }

    public void putAll( final Map<?, ?> map )
    {
        throw new UnsupportedOperationException();
    }

    public Object remove( final Object key )
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public Set<Object> keySet()
    {
        throw new UnsupportedOperationException();
    }

    public Collection<Object> values()
    {
        throw new UnsupportedOperationException();
    }

    public Set<Entry<Object, Object>> entrySet()
    {
        throw new UnsupportedOperationException();
    }
}
