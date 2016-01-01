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
package org.codehaus.plexus.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultPlexusConfiguration
    implements PlexusConfiguration
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final PlexusConfiguration[] NO_CHILDREN = new PlexusConfiguration[0];

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String name;

    private String value;

    private List<PlexusConfiguration> childIndex = Collections.emptyList();

    private Map<String, List<PlexusConfiguration>> childMap = Collections.emptyMap();

    private Map<String, String> attributeMap = Collections.emptyMap();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusConfiguration( final String name )
    {
        this( name, null );
    }

    public DefaultPlexusConfiguration( final String name, final String value )
    {
        this.name = name;
        this.value = value;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final String getName()
    {
        return name;
    }

    public final String getValue()
    {
        return value;
    }

    public final String getValue( final String defaultValue )
    {
        return null != value ? value : defaultValue;
    }

    public final void setValue( final String value )
    {
        this.value = value;
    }

    public final String[] getAttributeNames()
    {
        return attributeMap.keySet().toArray( new String[attributeMap.size()] );
    }

    public final String getAttribute( final String attributeName )
    {
        return attributeMap.get( attributeName );
    }

    public final String getAttribute( final String attributeName, final String defaultValue )
    {
        final String attributeValue = attributeMap.get( attributeName );
        return null != attributeValue ? attributeValue : defaultValue;
    }

    public final void setAttribute( final String attributeName, final String attributeValue )
    {
        if ( attributeMap.isEmpty() )
        {
            attributeMap = new HashMap<String, String>();
        }
        attributeMap.put( attributeName, attributeValue );
    }

    public final PlexusConfiguration getChild( final String childName )
    {
        return getChild( childName, true );
    }

    public final PlexusConfiguration getChild( final String childName, final boolean create )
    {
        final List<PlexusConfiguration> children = childMap.get( childName );
        if ( null != children )
        {
            return children.get( 0 );
        }
        return create ? add( createChild( childName ) ) : null;
    }

    public final PlexusConfiguration[] getChildren()
    {
        return childIndex.toArray( new PlexusConfiguration[childIndex.size()] );
    }

    public final PlexusConfiguration[] getChildren( final String childName )
    {
        final List<PlexusConfiguration> children = childMap.get( childName );
        if ( null != children )
        {
            return children.toArray( new PlexusConfiguration[children.size()] );
        }
        return NO_CHILDREN;
    }

    public final int getChildCount()
    {
        return childIndex.size();
    }

    public final PlexusConfiguration getChild( final int index )
    {
        return childIndex.get( index );
    }

    public final void addChild( final PlexusConfiguration child )
    {
        add( child );
    }

    public final PlexusConfiguration addChild( final String childName, final String childValue )
    {
        add( createChild( childName ) ).setValue( childValue );
        return this;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected PlexusConfiguration createChild( final String childName )
    {
        return new DefaultPlexusConfiguration( childName );
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected final PlexusConfiguration add( final PlexusConfiguration child )
    {
        final String childName = child.getName();

        List<PlexusConfiguration> children = childMap.get( childName );
        if ( null == children )
        {
            children = new ArrayList<PlexusConfiguration>();
            if ( childMap.isEmpty() )
            {
                // create mutable map and index at the same time
                childMap = new LinkedHashMap<String, List<PlexusConfiguration>>();
                childIndex = new ArrayList<PlexusConfiguration>();
            }
            childMap.put( childName, children );
        }

        childIndex.add( child );
        children.add( child );

        return child;
    }
}
