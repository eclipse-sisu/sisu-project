/*******************************************************************************
 * Copyright (c) 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.composite;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.reflect.BeanProperties;
import org.eclipse.sisu.reflect.BeanProperty;

import com.google.inject.TypeLiteral;

final class BeanHelper
{
    private static final Type[] NO_TYPES = {};

    private final ConverterLookup lookup;

    private final ClassLoader loader;

    private final ExpressionEvaluator evaluator;

    private final ConfigurationListener listener;

    BeanHelper( final ConverterLookup lookup, final ClassLoader loader, final ExpressionEvaluator evaluator,
                final ConfigurationListener listener )
    {
        this.lookup = lookup;
        this.loader = loader;
        this.evaluator = evaluator;
        this.listener = listener;
    }

    void setDefault( final Object bean, final Object defaultValue, final PlexusConfiguration cfg )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        ComponentConfigurationException problem = null;
        for ( final Method method : beanType.getMethods() )
        {
            if ( "set".equals( method.getName() ) && !Modifier.isStatic( method.getModifiers() ) )
            {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if ( parameterTypes.length == 1 )
                {
                    try
                    {
                        Object value = defaultValue;
                        final Class<?> type = parameterTypes[0];
                        if ( !type.isInstance( value ) )
                        {
                            value = convertProperty( beanType, type, null, cfg );
                        }
                        if ( null != listener )
                        {
                            listener.notifyFieldChangeUsingSetter( "", value, bean );
                        }
                        try
                        {
                            method.invoke( bean, value );
                            return;
                        }
                        catch ( final Exception e )
                        {
                            throw new ComponentConfigurationException( cfg, "Cannot set default", e );
                        }
                        catch ( final LinkageError e )
                        {
                            throw new ComponentConfigurationException( cfg, "Cannot set default", e );
                        }
                    }
                    catch ( final ComponentConfigurationException e )
                    {
                        if ( null == problem )
                        {
                            problem = e;
                        }
                    }
                }
            }
        }
        if ( null != problem )
        {
            throw problem;
        }
    }

    void setProperty( final Object bean, final String propertyName, final Class<?> implType,
                      final PlexusConfiguration cfg )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        ComponentConfigurationException problem = null;
        for ( final BeanProperty<Object> property : new BeanProperties( beanType ) )
        {
            if ( propertyName.equals( property.getName() ) )
            {
                final TypeLiteral<?> propertyType = property.getType();
                Class<?> rawPropertyType = propertyType.getRawType();
                if ( null != implType && rawPropertyType.isAssignableFrom( implType ) )
                {
                    rawPropertyType = implType; // pick more specific type
                }
                final Type genericPropertyType = propertyType.getType();
                try
                {
                    final Object value = convertProperty( beanType, rawPropertyType, genericPropertyType, cfg );
                    if ( null != value )
                    {
                        if ( null != listener )
                        {
                            listener.notifyFieldChangeUsingReflection( propertyName, value, bean );
                        }
                        property.set( bean, value );
                        return;
                    }
                }
                catch ( final ComponentConfigurationException e )
                {
                    if ( null == problem )
                    {
                        problem = e;
                    }
                }
            }
        }
        if ( null != problem )
        {
            throw problem;
        }
    }

    private Object convertProperty( final Class<?> beanType, final Class<?> rawPropertyType,
                                    final Type genericPropertyType, final PlexusConfiguration cfg )
        throws ComponentConfigurationException
    {
        final ConfigurationConverter converter = lookup.lookupConverterForType( rawPropertyType );
        if ( genericPropertyType != null && converter instanceof ParameterizedConfigurationConverter )
        {
            final Type[] paramTypes = getParameterTypes( genericPropertyType );
            return ( (ParameterizedConfigurationConverter) converter ).fromConfiguration( lookup, cfg, rawPropertyType,
                                                                                          paramTypes, beanType, loader,
                                                                                          evaluator, listener );
        }
        return converter.fromConfiguration( lookup, cfg, rawPropertyType, beanType, loader, evaluator, listener );
    }

    private static Type[] getParameterTypes( final Type type )
    {
        if ( type instanceof ParameterizedType )
        {
            final Type[] argumentTypes = ( (ParameterizedType) type ).getActualTypeArguments();
            for ( int i = 0; i < argumentTypes.length; i++ )
            {
                argumentTypes[i] = expandType( argumentTypes[i] );
            }
            return argumentTypes;
        }
        if ( type instanceof GenericArrayType )
        {
            return new Type[] { expandType( ( (GenericArrayType) type ).getGenericComponentType() ) };
        }
        return NO_TYPES;
    }

    private static Type expandType( final Type type )
    {
        if ( type instanceof WildcardType )
        {
            return ( (WildcardType) type ).getUpperBounds()[0];
        }
        if ( type instanceof TypeVariable<?> )
        {
            return ( (TypeVariable<?>) type ).getBounds()[0];
        }
        return type;
    }
}
