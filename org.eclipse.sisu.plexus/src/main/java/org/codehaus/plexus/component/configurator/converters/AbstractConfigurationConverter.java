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
package org.codehaus.plexus.component.configurator.converters;

import java.lang.reflect.Array;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.plexus.Roles;

@SuppressWarnings( "static-method" )
public abstract class AbstractConfigurationConverter
    implements ConfigurationConverter
{
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        return fromConfiguration( lookup, configuration, type, enclosingType, loader, evaluator, null );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * 
     * @param configuration
     * @param evaluator
     * @return
     * @throws ComponentConfigurationException
     * @deprecated Use {@link #fromExpression(PlexusConfiguration, ExpressionEvaluator, Class)} instead
     */
    @Deprecated
    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        return fromExpression( configuration, evaluator, null );
    }

    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                     final Class<?> type )
        throws ComponentConfigurationException
    {
        return fromExpression( configuration, evaluator, type, true );
    }

    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                     final Class<?> type, boolean enforceTypeCompatibility )
        throws ComponentConfigurationException

    {
        String value = configuration.getValue();
        try
        {
            Object result = null;
            if ( null != value && !value.isEmpty() )
            {
                if ( evaluator instanceof TypeAwareExpressionEvaluator && type != null )
                {
                    result = ( (TypeAwareExpressionEvaluator) evaluator ).evaluate( value, type );
                }
                else
                {
                    result = evaluator.evaluate( value );
                }
            }
            if ( null == result && configuration.getChildCount() == 0 )
            {
                value = configuration.getAttribute( "default-value" );
                if ( null != value && !value.isEmpty() )
                {
                    if ( evaluator instanceof TypeAwareExpressionEvaluator && type != null )
                    {
                        result = ( (TypeAwareExpressionEvaluator) evaluator ).evaluate( value, type );
                    }
                    else
                    {
                        result = evaluator.evaluate( value );
                    }
                }
            }
            if ( enforceTypeCompatibility && type != null )
            {
                failIfNotTypeCompatible( result, type, configuration );
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

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected final String fromXML( final String name )
    {
        return Roles.camelizeName( name );
    }

    protected final void failIfNotTypeCompatible( final Object value, final Class<?> type,
                                                  final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        if ( null != value && null != type && !type.isInstance( value ) )
        {
            final String reason =
                String.format( "Cannot assign configuration entry '%s' with value '%s' of type %s to property of type %s",
                               configuration.getName(), configuration.getValue(), value.getClass().getCanonicalName(),
                               type.getCanonicalName() );

            throw new ComponentConfigurationException( configuration, reason );
        }
    }

    protected final Class<?> getClassForImplementationHint( final Class<?> type,
                                                            final PlexusConfiguration configuration,
                                                            final ClassLoader loader )
        throws ComponentConfigurationException
    {
        String hint = configuration.getAttribute( "implementation" );
        if ( null == hint )
        {
            return type;
        }
        try
        {
            int dims = 0;
            for ( ; hint.endsWith( "[]" ); dims++ )
            {
                hint = hint.substring( 0, hint.length() - 2 );
            }
            Class<?> implType = loader.loadClass( hint );
            for ( ; dims > 0; dims-- )
            {
                implType = Array.newInstance( implType, 0 ).getClass();
            }
            return implType;
        }
        catch ( final Exception e )
        {
            throw new ComponentConfigurationException( "Cannot load implementation hint '" + hint + "'", e );
        }
        catch ( final LinkageError e )
        {
            throw new ComponentConfigurationException( "Cannot load implementation hint '" + hint + "'", e );
        }
    }

    protected final Object instantiateObject( final Class<?> type )
        throws ComponentConfigurationException
    {
        try
        {
            return type.newInstance();
        }
        catch ( final Exception e )
        {
            throw new ComponentConfigurationException( "Cannot create instance of " + type, e );
        }
        catch ( final LinkageError e )
        {
            throw new ComponentConfigurationException( "Cannot create instance of " + type, e );
        }
    }
}
