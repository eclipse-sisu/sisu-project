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

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class EnumConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return type.isEnum();
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        if ( configuration.getChildCount() > 0 )
        {
            throw new ComponentConfigurationException( "Basic element '" + configuration.getName()
                + "' must not contain child elements" );
        }

        Object result = fromExpression( configuration, evaluator );
        if ( result instanceof String )
        {
            try
            {
                result = Enum.valueOf( (Class) type, (String) result );
            }
            catch ( final RuntimeException e )
            {
                throw new ComponentConfigurationException( "Cannot convert '" + result + "' to Enum", e );
            }
        }
        return result;
    }
}
