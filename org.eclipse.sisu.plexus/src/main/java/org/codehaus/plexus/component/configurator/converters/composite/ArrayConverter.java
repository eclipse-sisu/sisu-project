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
package org.codehaus.plexus.component.configurator.converters.composite;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class ArrayConverter
    extends AbstractCollectionConverter
    implements ParameterizedConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return type.isArray();
    }

    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        return fromConfiguration( lookup, configuration, type, null, enclosingType, loader, evaluator, listener );
    }

    @SuppressWarnings( "unchecked" )
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Type[] typeArguments, final Class<?> enclosingType,
                                     final ClassLoader loader, final ExpressionEvaluator evaluator,
                                     final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final Object value = fromExpression( configuration, evaluator );
        if ( type.isInstance( value ) )
        {
            return value;
        }
        try
        {
            final Collection<Object> elements;
            final Type elementType = findElementType( type, typeArguments );
            if ( null == value )
            {
                elements = fromChildren( lookup, configuration, type, enclosingType, loader, evaluator, listener,
                                         elementType );
            }
            else if ( value instanceof String )
            {
                final PlexusConfiguration xml = csvToXml( configuration, (String) value );
                elements = fromChildren( lookup, xml, type, enclosingType, loader, evaluator, listener, elementType );
            }
            else if ( value instanceof Collection<?> )
            {
                elements = (Collection<Object>) value;
            }
            else
            {
                failIfNotTypeCompatible( value, type, configuration );
                elements = Collections.emptyList(); // unreachable
            }
            return elements.toArray( (Object[]) Array.newInstance( type.getComponentType(), elements.size() ) );
        }
        catch ( final ComponentConfigurationException e )
        {
            if ( null == e.getFailedConfiguration() )
            {
                e.setFailedConfiguration( configuration );
            }
            throw e;
        }
        catch ( final ArrayStoreException e )
        {
            throw new ComponentConfigurationException( configuration, "Cannot store value into array", e );
        }
    }

    @Override
    protected final Collection<Object> instantiateCollection( final PlexusConfiguration configuration,
                                                              final Class<?> type, final ClassLoader loader )
    {
        return new ArrayList<Object>( configuration.getChildCount() );
    }

    private static Type findElementType( final Class<?> rawArrayType, final Type[] typeArguments )
    {
        if ( null != typeArguments && typeArguments.length > 0 )
        {
            return typeArguments[0];
        }
        return rawArrayType.getComponentType();
    }
}
