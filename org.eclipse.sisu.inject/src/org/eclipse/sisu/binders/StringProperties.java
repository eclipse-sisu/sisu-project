/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.binders;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class StringProperties
    extends AbstractMap<String, String>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    StringProperties( final Map<?, ?> delegate )
    {
        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public String get( final Object key )
    {
        final Object value = delegate.get( key );
        if ( value instanceof String )
        {
            return (String) value;
        }
        return null;
    }

    @Override
    public boolean containsKey( final Object key )
    {
        final Object value = delegate.get( key );
        if ( null == value )
        {
            return delegate.containsKey( key );
        }
        return value instanceof String;
    }

    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Set<Entry<String, String>> entrySet()
    {
        final Set entries = new HashSet();
        for ( final Entry e : delegate.entrySet() )
        {
            if ( e.getKey() instanceof String )
            {
                final Object value = e.getValue();
                if ( null == value || value instanceof String )
                {
                    entries.add( e );
                }
            }
        }
        return entries;
    }
}
