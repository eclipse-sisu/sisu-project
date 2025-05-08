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
package org.eclipse.sisu.inject;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Description;

import com.google.inject.Binding;
import com.google.inject.Scopes;

/**
 * Lazy {@link BeanEntry} backed by a qualified {@link Binding} and an assigned rank.
 */
final class LazyBeanEntry<Q extends Annotation, T>
    implements BeanEntry<Q, T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Q qualifier;

    final Binding<T> binding;

    private final Provider<T> lazyValue;

    private final int rank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    LazyBeanEntry( final Q qualifier, final Binding<T> binding, final int rank )
    {
        if ( null != qualifier && com.google.inject.name.Named.class == qualifier.annotationType() )
        {
            this.qualifier = (Q) new JsrNamed( (com.google.inject.name.Named) qualifier );
        }
        else
        {
            this.qualifier = qualifier;
        }

        this.binding = binding;
        this.rank = rank;

        if ( Scopes.isSingleton( binding ) )
        {
            this.lazyValue = binding.getProvider();
        }
        else
        {
            this.lazyValue = Guice4.lazy( binding );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Q getKey()
    {
        return qualifier;
    }

    public T getValue()
    {
        return lazyValue.get();
    }

    public T setValue( final T value )
    {
        throw new UnsupportedOperationException();
    }

    public Provider<T> getProvider()
    {
        return binding.getProvider();
    }

    public String getDescription()
    {
        final Description description = Sources.getAnnotation( binding, Description.class );
        return null != description ? description.value() : null;
    }

    @SuppressWarnings( "unchecked" )
    public Class<T> getImplementationClass()
    {
        return (Class<T>) Implementations.find( binding );
    }

    public Object getSource()
    {
        return Guice4.getDeclaringSource( binding );
    }

    public int getRank()
    {
        return rank;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder().append( getKey() ).append( '=' );
        try
        {
            final Class<T> impl = getImplementationClass();
            buf.append( null != impl ? impl : getProvider() );
        }
        catch ( final RuntimeException e )
        {
            buf.append( e );
        }
        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Implementation of @{@link javax.inject.Named} that can also act like @{@link com.google.inject.name.Named}.
     */
    private static final class JsrNamed
        implements com.google.inject.name.Named, javax.inject.Named
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final String value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        JsrNamed( final com.google.inject.name.Named named )
        {
            value = named.value();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public String value()
        {
            return value;
        }

        public Class<? extends Annotation> annotationType()
        {
            return javax.inject.Named.class;
        }

        @Override
        public int hashCode()
        {
            return 127 * "value".hashCode() ^ value.hashCode();
        }

        @Override
        public boolean equals( final Object rhs )
        {
            if ( this == rhs )
            {
                return true;
            }
            if ( rhs instanceof com.google.inject.name.Named )
            {
                return value.equals( ( (com.google.inject.name.Named) rhs ).value() );
            }
            if ( rhs instanceof javax.inject.Named )
            {
                return value.equals( ( (javax.inject.Named) rhs ).value() );
            }
            return false;
        }

        @Override
        public String toString()
        {
            return "@" + javax.inject.Named.class.getName() + "(value=" + value + ")";
        }
    }
}
