/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.basic;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;

/**
 * Supports type conversion into {@link java.time} classes.
 * The supported patterns of the used {@link DateTimeFormatter} is either
 * <ul>
 * <li>ISO-8601 extended offset date-time-format ({@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}) or</li>
 * <li>{@code yyyy-MM-dd HH:mm:ss[[a][.S [a]]}</li>
 * </ul>
 *
 */
public class TemporalConverter
    extends AbstractBasicConverter
{
    /**
     * Supports all formats of {@link org.eclipse.sisu.plexus.PlexusDateTypeConverter}
     */
    private static final DateTimeFormatter PLEXUS_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern( "yyyy-MM-dd HH:mm:ss[[a][.S [a]]", Locale.US )
            .withZone( ZoneId.systemDefault() );

    public boolean canConvert( final Class<?> type )
    {
        return Temporal.class.isAssignableFrom( type );
    }

    @Override
    protected final Object fromString( final String str, final Class<?> type ) throws ComponentConfigurationException
    {
        return createTemporalFromString( str, type );
    }

    private Temporal createTemporalFromString( String value, final Class<?> type ) 
    {
        TemporalAccessor temporalAccessor;
        try 
        {
            temporalAccessor = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse( value );
        }
        catch ( DateTimeParseException e )
        {
            temporalAccessor = PLEXUS_DATE_TIME_FORMATTER.parse( value );
        }
        final Temporal temporal;
        if ( type.equals( LocalDate.class ) )
        {
            temporal = LocalDate.from( temporalAccessor );
        } 
        else if ( type.equals( LocalDateTime.class ) )
        {
            temporal = LocalDateTime.from( temporalAccessor );
        } 
        else if ( type.equals( LocalTime.class ) )
        {
            temporal = LocalTime.from( temporalAccessor );
        }
        else if ( type.equals( Instant.class ) )
        {
            temporal = Instant.from( temporalAccessor );
        }
        else if ( type.equals( OffsetDateTime.class ) )
        {
            temporal = ZonedDateTime.from( temporalAccessor ).toOffsetDateTime();
        }
        else if ( type.equals( OffsetTime.class ) )
        {
            temporal = ZonedDateTime.from( temporalAccessor ).toOffsetDateTime().toOffsetTime();
        }
        else if ( type.equals(ZonedDateTime.class ) )
        {
            temporal = ZonedDateTime.from( temporalAccessor );
        }
        else
        {
            throw new IllegalArgumentException( "Unsupported temporal type " + type );
        }
        return temporal;
    }
}
