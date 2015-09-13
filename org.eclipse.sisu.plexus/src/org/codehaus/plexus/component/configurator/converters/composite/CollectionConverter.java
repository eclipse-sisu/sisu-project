/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
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
package org.codehaus.plexus.component.configurator.converters.composite;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class CollectionConverter
    extends AbstractCollectionConverter
    implements ParameterizedConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return Collection.class.isAssignableFrom( type ) && !Map.class.isAssignableFrom( type );
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
        final Object value = fromExpression( configuration, evaluator );
        if ( type.isInstance( value ) )
        {
            return value;
        }
        try
        {
            final Collection<Object> elements;
            final Class<?> elementType = findElementType( typeArguments );
            if ( null == value )
            {
                elements =
                    fromChildren( lookup, configuration, type, enclosingType, loader, evaluator, listener, elementType );
            }
            else if ( value instanceof String )
            {
                final PlexusConfiguration xml = csvToXml( configuration, (String) value );
                elements = fromChildren( lookup, xml, type, enclosingType, loader, evaluator, listener, elementType );
            }
            else if ( value instanceof Object[] )
            {
                elements = instantiateCollection( configuration, type, loader );
                Collections.addAll( elements, (Object[]) value );
            }
            else
            {
                failIfNotTypeCompatible( value, type, configuration );
                elements = Collections.emptyList(); // unreachable
            }
            return elements;
        }
        catch ( final ComponentConfigurationException e )
        {
            if ( null == e.getFailedConfiguration() )
            {
                e.setFailedConfiguration( configuration );
            }
            throw e;
        }
        catch ( final IllegalArgumentException e )
        {
            throw new ComponentConfigurationException( configuration, "Cannot store value into collection", e );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected final Collection<Object> instantiateCollection( final PlexusConfiguration configuration,
                                                              final Class<?> type, final ClassLoader loader )
        throws ComponentConfigurationException
    {
        final Class<?> implType = getClassForImplementationHint( type, configuration, loader );
        if ( null == implType || Modifier.isAbstract( implType.getModifiers() ) )
        {
            if ( Set.class.isAssignableFrom( type ) )
            {
                if ( SortedSet.class.isAssignableFrom( type ) )
                {
                    return new TreeSet<Object>();
                }
                return new HashSet<Object>();
            }
            return new ArrayList<Object>();
        }

        final Object impl = instantiateObject( implType );
        failIfNotTypeCompatible( impl, type, configuration );
        return (Collection<Object>) impl;
    }

    private static Class<?> findElementType( final Type[] typeArguments )
    {
        if ( null != typeArguments && typeArguments.length > 0 && typeArguments[0] instanceof Class<?> )
        {
            return (Class<?>) typeArguments[0];
        }
        return Object.class;
    }
}
