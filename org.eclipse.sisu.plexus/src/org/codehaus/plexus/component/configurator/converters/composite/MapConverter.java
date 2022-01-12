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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.plexus.TypeArguments;

public class MapConverter
    extends AbstractConfigurationConverter
    implements ParameterizedConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return Map.class.isAssignableFrom( type ) && !Properties.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        return fromConfiguration( lookup, configuration, type, null, enclosingType, loader, evaluator, listener );
    }

    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Type[] typeArguments, final Class<?> enclosingType,
                                     final ClassLoader loader, final ExpressionEvaluator evaluator,
                                     final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final Object value = fromExpression( configuration, evaluator, type );
        if ( null != value )
        {
            return value;
        }
        try
        {
            final Map<Object, Object> map = instantiateMap( configuration, type, loader );
            final Type elementType = findElementType( typeArguments );
            if ( Object.class == elementType || String.class == elementType )
            {
                for ( int i = 0, size = configuration.getChildCount(); i < size; i++ )
                {
                    final PlexusConfiguration element = configuration.getChild( i );
                    map.put( element.getName(), fromExpression( element, evaluator ) );
                }
                return map;
            }
            // handle maps with complex element types...
            final Class<?> rawElementType = TypeArguments.getRawType( elementType );
            final ConfigurationConverter c = lookup.lookupConverterForType( rawElementType );
            final ParameterizedConfigurationConverter pc = rawElementType != elementType
                && c instanceof ParameterizedConfigurationConverter ? (ParameterizedConfigurationConverter) c : null;
            for ( int i = 0, size = configuration.getChildCount(); i < size; i++ )
            {
                Object elementValue;
                final PlexusConfiguration element = configuration.getChild( i );
                try
                {
                    if ( null != pc )
                    {
                        elementValue = pc.fromConfiguration( lookup, element, rawElementType, //
                                                             TypeArguments.get( elementType ), enclosingType, //
                                                             loader, evaluator, listener );
                    }
                    else
                    {
                        elementValue = c.fromConfiguration( lookup, element, rawElementType, enclosingType, //
                                                            loader, evaluator, listener );
                    }
                }
                // TEMP: remove when http://jira.codehaus.org/browse/MSHADE-168 is fixed
                catch ( final ComponentConfigurationException e )
                {
                    elementValue = fromExpression( element, evaluator );

                    Logs.warn( "Map in " + enclosingType + " declares value type as: {} but saw: {} at runtime",
                               elementType, null != elementValue ? elementValue.getClass() : null );
                }
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                map.put( element.getName(), elementValue );
            }
            return map;
        }
        catch ( final ComponentConfigurationException e )
        {
            if ( null == e.getFailedConfiguration() )
            {
                e.setFailedConfiguration( configuration );
            }
            throw e;
        }
    }

    @SuppressWarnings( "unchecked" )
    private Map<Object, Object> instantiateMap( final PlexusConfiguration configuration, final Class<?> type,
                                                final ClassLoader loader )
        throws ComponentConfigurationException
    {
        final Class<?> implType = getClassForImplementationHint( type, configuration, loader );
        if ( null == implType || Modifier.isAbstract( implType.getModifiers() ) )
        {
            return new TreeMap<Object, Object>();
        }

        final Object impl = instantiateObject( implType );
        failIfNotTypeCompatible( impl, type, configuration );
        return (Map<Object, Object>) impl;
    }

    private static Type findElementType( final Type[] typeArguments )
    {
        if ( null != typeArguments && typeArguments.length > 1 )
        {
            return typeArguments[1];
        }
        return Object.class;
    }
}
