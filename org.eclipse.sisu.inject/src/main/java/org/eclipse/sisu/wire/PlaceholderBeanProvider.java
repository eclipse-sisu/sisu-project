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
package org.eclipse.sisu.wire;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.sisu.Parameters;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeConverter;

/**
 * Provides a single bean; the name used to lookup/convert the bean is selected at runtime.
 */
final class PlaceholderBeanProvider<V>
    implements Provider<V>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int EXPRESSION_RECURSION_LIMIT = 8;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    @Parameters
    @SuppressWarnings( "rawtypes" )
    private Map properties;

    @Inject
    private TypeConverterCache converterCache;

    private final BeanProviders beans;

    private final Key<V> placeholderKey;

    private volatile Entry<Key<?>, Provider<?>> cachedLookup; // NOSONAR

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlaceholderBeanProvider( final BeanProviders beans, final Key<V> key )
    {
        this.beans = beans;
        placeholderKey = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public V get()
    {
        final String template = ( (Named) placeholderKey.getAnnotation() ).value();
        final TypeLiteral<V> expectedType = placeholderKey.getTypeLiteral();

        // ---------------- INTERPOLATION ----------------

        final Class<?> clazz = expectedType.getRawType();
        Object value = interpolate( template, clazz );
        if ( false == value instanceof String )
        {
            return (V) value; // found non-String mapping
        }

        // ------------------- LOOKUP --------------------

        final Key<V> lookupKey = Key.get( expectedType, Names.named( (String) value ) );
        if ( String.class != clazz )
        {
            final V bean = lookup( lookupKey );
            if ( null != bean )
            {
                return bean; // found non-String binding
            }
        }

        // ----------------- CONVERSION ------------------

        if ( template == value ) // NOPMD we want to know if same instance
        {
            // no interpolation occurred; is this perhaps a Guice constant?
            value = nullify( lookup( lookupKey.ofType( String.class ) ) );
        }
        if ( null == value || String.class == clazz )
        {
            return (V) value; // no conversion required
        }
        final TypeConverter converter = converterCache.getTypeConverter( expectedType );
        if ( null != converter )
        {
            return (V) converter.convert( (String) value, expectedType );
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private <T> T lookup( final Key<T> key )
    {
        Entry<Key<?>, Provider<?>> lookup = cachedLookup;
        if ( null == lookup || !key.equals( lookup.getKey() ) )
        {
            lookup = new SimpleImmutableEntry( key, beans.firstOf( key ) );
            cachedLookup = lookup;
        }
        return (T) lookup.getValue().get();
    }

    private static String nullify( final String value )
    {
        return "null".equals( value ) ? null : value;
    }

    private Object interpolate( final String template, final Class<?> clazz )
    {
        final StringBuilder buf;
        if ( template.contains( "${" ) )
        {
            buf = new StringBuilder( template );
        }
        else if ( properties.containsKey( template ) )
        {
            // handle situations where someone missed out the main brackets
            buf = new StringBuilder( "${" ).append( template ).append( '}' );
        }
        else
        {
            return template; // nothing to interpolate, maintain reference
        }
        int x = 0, y, expressionEnd = 0, expressionNum = 0;
        while ( ( x = buf.indexOf( "${", x ) ) >= 0 && ( y = buf.indexOf( "}", x ) + 1 ) > 0 )
        {
            if ( y > expressionEnd ) // making progress
            {
                expressionNum = 0;
                expressionEnd = y;
            }
            final String key = buf.substring( x + 2, y - 1 );
            final int anchor = key.indexOf( ":-" );
            Object value = properties.get( anchor < 0 ? key : key.substring( 0, anchor ) );
            if ( value == null && anchor >= 0 )
            {
                value = key.substring( anchor + 2 );
            }
            if ( expressionNum++ >= EXPRESSION_RECURSION_LIMIT )
            {
                throw new ProvisionException( "Recursive configuration: " + template + " stopped at: " + buf );
            }
            final int len = buf.length();
            if ( 0 == x && len == y && String.class != clazz && clazz.isInstance( value ) )
            {
                return value; // found compatible (non-String) instance in the properties!
            }
            buf.replace( x, y, String.valueOf( value ) );
            expressionEnd += buf.length() - len;
        }
        return nullify( buf.toString() );
    }
}
