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
        final Object value = fromExpression( configuration, evaluator, type );
        if ( null != value )
        {
            return value;
        }
        try
        {
            final Properties properties = instantiateProperties( configuration, type, loader );
            for ( int i = 0, size = configuration.getChildCount(); i < size; i++ )
            {
                final PlexusConfiguration element = configuration.getChild( i );
                if ( element.getChildCount() > 0 )
                {
                    final Object name = fromExpression( element.getChild( "name" ), evaluator );
                    setProperty( properties, name, element.getChild( "value" ), evaluator );
                }
                else
                {
                    setProperty( properties, element.getName(), element, evaluator );
                }
            }
            return properties;
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

    private Properties instantiateProperties( final PlexusConfiguration configuration, final Class<?> type,
                                              final ClassLoader loader )
        throws ComponentConfigurationException
    {
        final Class<?> implType = getClassForImplementationHint( type, configuration, loader );
        if ( null == implType || Modifier.isAbstract( implType.getModifiers() ) )
        {
            return new Properties();
        }

        final Object impl = instantiateObject( implType );
        failIfNotTypeCompatible( impl, type, configuration );
        return (Properties) impl;
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
            throw new ComponentConfigurationException( "Missing name in properties" );
        }
    }
}
