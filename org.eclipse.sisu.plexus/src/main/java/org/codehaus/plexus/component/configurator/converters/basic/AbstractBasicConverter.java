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
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public abstract class AbstractBasicConverter
    extends AbstractConfigurationConverter
{
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

        Object result = fromExpression( configuration, evaluator, type );
        if ( result instanceof String )
        {
            try
            {
                result = fromString( (String) result, type );
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
        return result;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected Object fromString( final String str, final Class<?> type )
            throws ComponentConfigurationException
    {
        return fromString( str );
    }

    protected Object fromString( final String str )
        throws ComponentConfigurationException 
    {
        throw new UnsupportedOperationException("The class " + this.getClass().getName() + " must implement one of the fromString(...) methods, but it doesn't");
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    @Override
    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                           final Class<?> type )
        throws ComponentConfigurationException
    {
        String value = configuration.getValue();
        try
        {
            Object result = null;
            if ( null != value && value.length() > 0 )
            {
                if ( evaluator instanceof TypeAwareExpressionEvaluator )
                {
                    result = ( (TypeAwareExpressionEvaluator) evaluator ).evaluate( value, type );
                }
                else
                {
                    result = evaluator.evaluate( value );
                }
            }
            if ( null == result )
            {
                value = configuration.getAttribute( "default-value" );
                if ( null != value && value.length() > 0 )
                {
                    if ( evaluator instanceof TypeAwareExpressionEvaluator )
                    {
                        result = ( (TypeAwareExpressionEvaluator) evaluator ).evaluate( value, type );
                    }
                    else
                    {
                        result = evaluator.evaluate( value );
                    }
                }
            }
            return result;
        }
        catch ( final ExpressionEvaluationException e )
        {
            final String reason = String.format( "Cannot evaluate expression '%s' for configuration entry '%s'", value,
                                                 configuration.getName() );

            throw new ComponentConfigurationException( configuration, reason, e );
        }
    }
}
