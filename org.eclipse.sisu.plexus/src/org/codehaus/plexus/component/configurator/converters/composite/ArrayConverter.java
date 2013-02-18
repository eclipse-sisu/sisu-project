/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.composite;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

public class ArrayConverter
    extends AbstractConfigurationConverter
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
        final Object value = fromExpression( configuration, evaluator );
        if ( type.isInstance( value ) )
        {
            return value;
        }
        try
        {
            final Collection<?> collection;
            if ( value instanceof Collection<?> )
            {
                collection = (Collection<?>) value;
            }
            else
            {
                collection = new ArrayList<Object>( configuration.getChildCount() );
                final CollectionHelper helper = new CollectionHelper( lookup, loader, evaluator, listener );
                final Type[] parameterTypes = { type.getComponentType() };
                if ( null == value )
                {
                    helper.addAll( collection, parameterTypes, enclosingType, configuration );
                }
                else if ( value instanceof String && ( "".equals( value ) || !value.equals( configuration.getValue() ) ) )
                {
                    helper.addAll( collection, parameterTypes, enclosingType, csvToXml( configuration, (String) value ) );
                }
                else
                {
                    failIfNotTypeCompatible( value, type, configuration );
                }
            }
            return collection.toArray( (Object[]) Array.newInstance( type.getComponentType(), collection.size() ) );
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

    private static PlexusConfiguration csvToXml( final PlexusConfiguration configuration, final String csv )
    {
        final PlexusConfiguration xml = new XmlPlexusConfiguration( configuration.getName() );
        for ( final String token : csv.split( ",", -1 ) )
        {
            xml.addChild( "#", token );
        }
        return xml;
    }
}
