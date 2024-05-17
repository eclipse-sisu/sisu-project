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
package org.codehaus.plexus.component.configurator.converters.basic;

import java.util.Date;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.eclipse.sisu.plexus.PlexusDateTypeConverter;

import com.google.inject.spi.TypeConverter;

public class DateConverter
    extends AbstractBasicConverter
{
    private static final TypeConverter DATE_CONVERTER = new PlexusDateTypeConverter();

    public boolean canConvert( final Class<?> type )
    {
        return Date.class.equals( type );
    }

    @Override
    public Object fromString( final String value )
        throws ComponentConfigurationException
    {
        try
        {
            return DATE_CONVERTER.convert( value, null /* unused */ );
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentConfigurationException( "Cannot convert '" + value + "' to Date" );
        }
    }
}
