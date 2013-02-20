/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.basic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

public class DateConverter
    extends AbstractBasicConverter
{
    private static final DateFormat[] PLEXUS_DATE_FORMATS = { new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.S a" ),
        new SimpleDateFormat( "yyyy-MM-dd hh:mm:ssa" ), new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" ),
        new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) };

    public boolean canConvert( final Class<?> type )
    {
        return Date.class.equals( type );
    }

    @Override
    public Object fromString( final String value )
        throws ComponentConfigurationException
    {
        for ( final DateFormat f : PLEXUS_DATE_FORMATS )
        {
            try
            {
                synchronized ( f ) // formats are not thread-safe!
                {
                    return f.parse( value );
                }
            }
            catch ( final ParseException e )
            {
                continue; // try another format
            }
        }
        throw new ComponentConfigurationException( "Cannot convert '" + value + "' to Date" );
    }
}
