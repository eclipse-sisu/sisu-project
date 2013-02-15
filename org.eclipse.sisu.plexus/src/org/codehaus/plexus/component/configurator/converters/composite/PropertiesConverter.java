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

import java.util.Properties;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class PropertiesConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return Properties.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object result = fromExpression( configuration, evaluator, type );
        if ( null == result )
        {
            result = fromChildren( configuration, evaluator );
        }
        return result;
    }

    private Object fromChildren( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        try
        {
            final Properties result = new Properties();
            for ( int i = 0, size = configuration.getChildCount(); i < size; i++ )
            {
                final PlexusConfiguration entry = configuration.getChild( i );
                if ( "property".equals( entry.getName() ) && entry.getChildCount() > 0 )
                {
                    final Object name = fromExpression( entry.getChild( "name" ), evaluator );
                    setProperty( result, name, entry.getChild( "value" ), evaluator );
                }
                else if ( entry.getChildCount() == 0 )
                {
                    setProperty( result, entry.getName(), entry, evaluator );
                }
            }
            return result;
        }
        catch ( final IllegalArgumentException e )
        {
            throw new ComponentConfigurationException( configuration, "Missing name in properties" );
        }
    }

    private void setProperty( final Properties properties, final Object name,
                              final PlexusConfiguration valueConfiguration, //
                              final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        final String key = null != name ? name.toString() : null;
        if ( null != key )
        {
            final Object value = fromExpression( valueConfiguration, evaluator );
            properties.setProperty( key, null != value ? value.toString() : "" );
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
