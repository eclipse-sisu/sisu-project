/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a single-parameter setter {@link Method}.
 */
final class BeanPropertySetter<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method method;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertySetter( final Method method )
    {
        this.method = method;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
    {
        return method.getAnnotation( annotationType );
    }

    @SuppressWarnings( "unchecked" )
    public TypeLiteral<T> getType()
    {
        return (TypeLiteral<T>) TypeLiteral.get( method.getGenericParameterTypes()[0] );
    }

    public String getName()
    {
        final String name = method.getName();

        // this is guaranteed OK by the checks made in the BeanProperties code
        return Character.toLowerCase( name.charAt( 3 ) ) + name.substring( 4 );
    }

    public <B> void set( final B bean, final T value )
    {
        if ( !method.isAccessible() )
        {
            // ensure we can update the property
            AccessController.doPrivileged( this );
        }

        try
        {
            method.invoke( bean, value );
        }
        catch ( final Exception e )
        {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            throw new ProvisionException( "Error injecting: " + method, cause );
        }
        catch ( final LinkageError e )
        {
            throw new ProvisionException( "Error injecting: " + method, e );
        }
    }

    @Override
    public int hashCode()
    {
        return method.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof BeanPropertySetter<?> )
        {
            return method.equals( ( (BeanPropertySetter<?>) rhs ).method );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return method.toString();
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        // enable private injection
        method.setAccessible( true );
        return null;
    }
}
