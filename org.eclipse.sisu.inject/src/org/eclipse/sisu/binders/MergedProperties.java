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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class MergedProperties
    extends AbstractMap<Object, Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?>[] properties;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    MergedProperties( final List<Map<?, ?>> properties )
    {
        this.properties = properties.toArray( new Map[properties.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Object get( final Object key )
    {
        for ( final Map<?, ?> p : properties )
        {
            final Object value = p.get( key );
            if ( null != value )
            {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey( final Object key )
    {
        for ( final Map<?, ?> p : properties )
        {
            if ( p.containsKey( key ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Set<Entry<Object, Object>> entrySet()
    {
        final Set entries = new LinkedHashSet();
        for ( final Map<?, ?> p : properties )
        {
            entries.addAll( p.entrySet() );
        }
        return entries;
    }
}
