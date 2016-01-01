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

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.plexus.CompositeBeanHelper;

public class ObjectWithFieldsConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return !Map.class.isAssignableFrom( type ) //
            && !Collection.class.isAssignableFrom( type ) //
            && !Dictionary.class.isAssignableFrom( type );
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
            final Class<?> implType = getClassForImplementationHint( type, configuration, loader );
            if ( null == value && implType.isInterface() && configuration.getChildCount() == 0 )
            {
                return null; // nothing to process
            }
            final Object bean = instantiateObject( implType );
            if ( null == value )
            {
                processConfiguration( lookup, bean, loader, configuration, evaluator, listener );
            }
            else
            {
                new CompositeBeanHelper( lookup, loader, evaluator, listener ).setDefault( bean, value, configuration );
            }
            return bean;
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

    public void processConfiguration( final ConverterLookup lookup, final Object bean, final ClassLoader loader,
                                      final PlexusConfiguration configuration, final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        processConfiguration( lookup, bean, loader, configuration, evaluator, null );
    }

    public void processConfiguration( final ConverterLookup lookup, final Object bean, final ClassLoader loader,
                                      final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                      final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final CompositeBeanHelper helper = new CompositeBeanHelper( lookup, loader, evaluator, listener );
        for ( int i = 0, size = configuration.getChildCount(); i < size; i++ )
        {
            final PlexusConfiguration element = configuration.getChild( i );
            final String propertyName = fromXML( element.getName() );
            Class<?> valueType;
            try
            {
                valueType = getClassForImplementationHint( null, element, loader );
            }
            catch ( final ComponentConfigurationException e )
            {
                valueType = null;
            }
            helper.setProperty( bean, propertyName, valueType, element );
        }
    }
}
