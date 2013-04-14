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
package org.eclipse.sisu.plexus;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ParameterizedConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.bean.DeclaredMembers;
import org.eclipse.sisu.bean.DeclaredMembers.View;

import com.google.inject.TypeLiteral;

public final class CompositeBeanHelper
{
    private static final Type[] NO_TYPES = {};

    private final ConverterLookup lookup;

    private final ClassLoader loader;

    private final ExpressionEvaluator evaluator;

    private final ConfigurationListener listener;

    public CompositeBeanHelper( final ConverterLookup lookup, final ClassLoader loader,
                                final ExpressionEvaluator evaluator, final ConfigurationListener listener )
    {
        this.lookup = lookup;
        this.loader = loader;
        this.evaluator = evaluator;
        this.listener = listener;
    }

    public void setDefault( final Object bean, final Object defaultValue, final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        final Type[] paramTypeHolder = new Type[1];

        // ----------------------------------------------------------------------

        final Method setter = findMethod( beanType, paramTypeHolder, "set" );
        if ( null == setter )
        {
            throw new ComponentConfigurationException( configuration, "Cannot find default setter in " + beanType );
        }

        // ----------------------------------------------------------------------

        Object value = defaultValue;
        final TypeLiteral<?> paramType = TypeLiteral.get( paramTypeHolder[0] );
        if ( !paramType.getRawType().isInstance( value ) )
        {
            if ( configuration.getChildCount() > 0 )
            {
                throw new ComponentConfigurationException( "Basic element '" + configuration.getName()
                    + "' must not contain child elements" );
            }
            value = convertProperty( beanType, paramType.getRawType(), paramType.getType(), configuration );
        }

        // ----------------------------------------------------------------------

        if ( null != value )
        {
            try
            {
                if ( null != listener )
                {
                    listener.notifyFieldChangeUsingSetter( "", value, bean );
                }
                setter.invoke( bean, value );
            }
            catch ( final Exception e )
            {
                throw new ComponentConfigurationException( configuration, "Cannot set default", e );
            }
            catch ( final LinkageError e )
            {
                throw new ComponentConfigurationException( configuration, "Cannot set default", e );
            }
        }
    }

    public void setProperty( final Object bean, final String propertyName, final Class<?> implType,
                             final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        final Type[] paramTypeHolder = new Type[1];

        // ----------------------------------------------------------------------

        final String title = Character.toTitleCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
        Method setter = findMethod( beanType, paramTypeHolder, "set" + title );
        if ( null == setter )
        {
            setter = findMethod( beanType, paramTypeHolder, "add" + title );
        }

        // ----------------------------------------------------------------------

        Throwable problem = null;
        Object value = null;

        if ( null != setter )
        {
            try
            {
                final TypeLiteral<?> paramType = TypeLiteral.get( paramTypeHolder[0] );
                Class<?> rawPropertyType = paramType.getRawType();
                if ( null != implType && rawPropertyType.isAssignableFrom( implType ) )
                {
                    rawPropertyType = implType; // pick more specific type
                }
                value = convertProperty( beanType, rawPropertyType, paramType.getType(), configuration );
                if ( null != value )
                {
                    if ( null != listener )
                    {
                        listener.notifyFieldChangeUsingSetter( propertyName, value, bean );
                    }
                    setter.invoke( bean, value );
                    return;
                }
            }
            catch ( final Exception e )
            {
                problem = e;
            }
            catch ( final LinkageError e )
            {
                problem = e;
            }
        }

        // ----------------------------------------------------------------------

        final Field field = findField( beanType, propertyName );
        if ( null != field )
        {
            try
            {
                final TypeLiteral<?> fieldType = TypeLiteral.get( field.getGenericType() );
                Class<?> rawPropertyType = fieldType.getRawType();
                if ( !rawPropertyType.isInstance( value ) ) // only re-convert if we must
                {
                    if ( null != implType && rawPropertyType.isAssignableFrom( implType ) )
                    {
                        rawPropertyType = implType; // pick more specific type
                    }
                    value = convertProperty( beanType, rawPropertyType, fieldType.getType(), configuration );
                }
                if ( null != value )
                {
                    if ( null != listener )
                    {
                        listener.notifyFieldChangeUsingReflection( propertyName, value, bean );
                    }
                    setField( bean, field, value );
                    return;
                }
            }
            catch ( final Exception e )
            {
                if ( null == problem )
                {
                    problem = e;
                }
            }
            catch ( final LinkageError e )
            {
                if ( null == problem )
                {
                    problem = e;
                }
            }
        }

        // ----------------------------------------------------------------------

        if ( problem instanceof ComponentConfigurationException )
        {
            throw (ComponentConfigurationException) problem;
        }
        if ( null != problem )
        {
            final String reason = "Cannot set '" + propertyName + "' in " + beanType;
            throw new ComponentConfigurationException( configuration, reason, problem );
        }
        if ( null == setter && null == field )
        {
            final String reason = "Cannot find '" + propertyName + "' in " + beanType;
            throw new ComponentConfigurationException( configuration, reason );
        }
    }

    private Object convertProperty( final Class<?> beanType, final Class<?> rawPropertyType,
                                    final Type genericPropertyType, final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final ConfigurationConverter converter = lookup.lookupConverterForType( rawPropertyType );
        if ( !( genericPropertyType instanceof Class<?> ) && converter instanceof ParameterizedConfigurationConverter )
        {
            final Type[] paramTypes = getParameterTypes( genericPropertyType );
            return ( (ParameterizedConfigurationConverter) converter ).fromConfiguration( lookup, configuration,
                                                                                          rawPropertyType, paramTypes,
                                                                                          beanType, loader, evaluator,
                                                                                          listener );
        }
        return converter.fromConfiguration( lookup, configuration, rawPropertyType, beanType, loader, evaluator,
                                            listener );
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

    private static Method findMethod( final Class<?> beanType, final Type[] paramTypeHolder, final String methodName )
    {
        for ( final Method m : beanType.getMethods() )
        {
            if ( methodName.equals( m.getName() ) && !Modifier.isStatic( m.getModifiers() ) )
            {
                final Type[] paramTypes = m.getGenericParameterTypes();
                if ( paramTypes.length == 1 )
                {
                    paramTypeHolder[0] = paramTypes[0];
                    return m;
                }
            }
        }
        return null;
    }

    private static Field findField( final Class<?> beanType, final String fieldName )
    {
        for ( final Member f : new DeclaredMembers( beanType, View.FIELDS ) )
        {
            if ( fieldName.equals( f.getName() ) && !Modifier.isStatic( f.getModifiers() ) )
            {
                return (Field) f;
            }
        }
        return null;
    }

    private static void setField( final Object bean, final Field field, final Object value )
        throws Exception
    {
        if ( !field.isAccessible() )
        {
            AccessController.doPrivileged( new PrivilegedAction<Void>()
            {
                public Void run()
                {
                    field.setAccessible( true );
                    return null;
                }
            } );
        }
        field.set( bean, value );
    }
}
