/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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

/**
 * Helper class that implements low-level Plexus configuration of composite beans.
 */
public final class CompositeBeanHelper
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConverterLookup lookup;

    private final ClassLoader loader;

    private final ExpressionEvaluator evaluator;

    private final ConfigurationListener listener;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public CompositeBeanHelper( final ConverterLookup lookup, final ClassLoader loader,
                                final ExpressionEvaluator evaluator, final ConfigurationListener listener )
    {
        this.lookup = lookup;
        this.loader = loader;
        this.evaluator = evaluator;
        this.listener = listener;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Calls the default "set" method on the bean; re-converts the configuration if necessary.
     * 
     * @param bean The bean being configured
     * @param defaultValue The default value
     * @param configuration The configuration
     */
    public void setDefault( final Object bean, final Object defaultValue, final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        final Type[] paramTypeHolder = new Type[1];

        // ----------------------------------------------------------------------

        final Method setter = findMethod( beanType, paramTypeHolder, "set", null );
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

    /**
     * Sets a property in the bean; looks for public setter/adder method before checking fields.
     * 
     * @param bean The bean being configured
     * @param propertyName The property name
     * @param valueType The value type
     * @param configuration The configuration
     */
    public void setProperty( final Object bean, final String propertyName, final Class<?> valueType,
                             final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final Class<?> beanType = bean.getClass();
        final Type[] paramTypeHolder = new Type[1];

        // ----------------------------------------------------------------------

        final String title = Character.toTitleCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
        Method setter = findMethod( beanType, paramTypeHolder, "set" + title, valueType );
        if ( null == setter )
        {
            setter = findMethod( beanType, paramTypeHolder, "add" + title, valueType );
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
                if ( null != valueType && rawPropertyType.isAssignableFrom( valueType ) )
                {
                    rawPropertyType = valueType; // pick more specific type
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
                    if ( null != valueType && rawPropertyType.isAssignableFrom( valueType ) )
                    {
                        rawPropertyType = valueType; // pick more specific type
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Object convertProperty( final Class<?> beanType, final Class<?> rawPropertyType,
                                    final Type genericPropertyType, final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        final ConfigurationConverter converter = lookup.lookupConverterForType( rawPropertyType );
        if ( !( genericPropertyType instanceof Class<?> ) && converter instanceof ParameterizedConfigurationConverter )
        {
            final Type[] propertyTypeArgs = TypeArguments.get( genericPropertyType );
            return ( (ParameterizedConfigurationConverter) converter ).fromConfiguration( lookup, configuration,
                                                                                          rawPropertyType,
                                                                                          propertyTypeArgs, beanType,
                                                                                          loader, evaluator, listener );
        }
        return converter.fromConfiguration( lookup, configuration, rawPropertyType, beanType, loader, evaluator,
                                            listener );
    }

    private static Method findMethod( final Class<?> beanType, final Type[] paramTypeHolder, final String methodName,
                               final Class<?> valueType )
    {
        Method candidate = null;
        for ( final Method m : beanType.getMethods() )
        {
            if ( methodName.equals( m.getName() ) && !Modifier.isStatic( m.getModifiers() ) )
            {
                final Type[] paramTypes = m.getGenericParameterTypes();
                if ( paramTypes.length == 1 )
                {
                    if ( valueType != null )
                    {
                        if ( m.getParameters()[0].getType().isAssignableFrom( valueType ) )
                        {
                            paramTypeHolder[0] = paramTypes[0];
                            return m;
                        }
                    }
                    // backward compat we keep returning the first method found
                    if ( candidate == null )
                    {
                        paramTypeHolder[0] = paramTypes[0];
                        candidate = m;
                    }
                }
            }
        }
        return candidate;
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
