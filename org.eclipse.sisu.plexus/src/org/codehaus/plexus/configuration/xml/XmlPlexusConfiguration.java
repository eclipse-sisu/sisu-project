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
package org.codehaus.plexus.configuration.xml;

import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public final class XmlPlexusConfiguration
    extends DefaultPlexusConfiguration
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public XmlPlexusConfiguration( final String name )
    {
        super( name );
    }

    public XmlPlexusConfiguration( final Xpp3Dom dom )
    {
        super( dom.getName(), dom.getValue() );

        for ( final String attribute : dom.getAttributeNames() )
        {
            setAttribute( attribute, dom.getAttribute( attribute ) );
        }

        for ( final Xpp3Dom child : dom.getChildren() )
        {
            addChild( new XmlPlexusConfiguration( child ) );
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder().append( '<' ).append( getName() );
        for ( final String a : getAttributeNames() )
        {
            buf.append( ' ' ).append( a ).append( "=\"" ).append( getAttribute( a ) ).append( '"' );
        }
        if ( getChildCount() > 0 )
        {
            buf.append( '>' );
            for ( int i = 0, size = getChildCount(); i < size; i++ )
            {
                buf.append( getChild( i ) );
            }
            buf.append( "</" ).append( getName() ).append( '>' );
        }
        else if ( null != getValue() )
        {
            buf.append( '>' ).append( getValue() ).append( "</" ).append( getName() ).append( '>' );
        }
        else
        {
            buf.append( "/>" );
        }
        return buf.append( '\n' ).toString();
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    @Override
    protected PlexusConfiguration createChild( final String name )
    {
        return new XmlPlexusConfiguration( name );
    }
}
