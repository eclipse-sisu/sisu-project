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
package org.codehaus.plexus.context;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContextMapAdapter
    extends AbstractMap<Object, Object>
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

    @Override
    public boolean containsKey( final Object key )
    {
        return get( key ) != null;
    }

    @Override
    public Object get( final Object key )
    {
        final Object value = contextData.get( key );
        return value instanceof String ? value : null;
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    @Override
    public Set<Entry<Object, Object>> entrySet()
    {
        final Set<Entry<Object, Object>> tempSet = new HashSet<Entry<Object, Object>>();
        for ( final Entry<Object, Object> entry : contextData.entrySet() )
        {
            if ( entry.getValue() instanceof String )
            {
                tempSet.add( entry );
            }
        }
        return tempSet;
    }
}
