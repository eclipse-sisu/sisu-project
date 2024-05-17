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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultContext
    implements Context
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    protected final Map<Object, Object> contextData = //
        new ConcurrentHashMap<Object, Object>( 16, 0.75f, 1 );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultContext()
    {
        // nothing to store
    }

    public DefaultContext( final Map<?, ?> context )
    {
        if ( null != context )
        {
            for ( final Entry<?, ?> e : context.entrySet() )
            {
                put( e.getKey(), e.getValue() );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean contains( final Object key )
    {
        return contextData.containsKey( key );
    }

    public void put( final Object key, final Object value )
    {
        if ( null == key )
        {
            throw new IllegalArgumentException( "Key is null" );
        }
        if ( null != value )
        {
            contextData.put( key, value );
        }
        else
        {
            contextData.remove( key );
        }
    }

    public Object get( final Object key )
        throws ContextException
    {
        final Object data = contextData.get( key );
        if ( data == null )
        {
            throw new ContextException( "Unable to resolve context key: " + key );
        }
        return data;
    }

    public Map<Object, Object> getContextData()
    {
        return Collections.unmodifiableMap( contextData );
    }

    @Override
    public String toString()
    {
        return contextData.toString();
    }
}
