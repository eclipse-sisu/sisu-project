/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
