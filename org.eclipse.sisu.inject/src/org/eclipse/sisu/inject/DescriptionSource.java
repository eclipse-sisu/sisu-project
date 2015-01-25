/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import java.lang.annotation.Annotation;

import org.eclipse.sisu.Description;

import com.google.inject.Binding;

/**
 * Implementation of @{@link Description} that can also act as an @{@link AnnotatedSource}.
 */
final class DescriptionSource
    implements Description, AnnotatedSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Object source;

    private final String value;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * @param source The owning source
     * @param value The description
     */
    DescriptionSource( final Object source, final String value )
    {
        this.source = source;
        this.value = value;
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
        return Description.class;
    }

    @Override
    public int hashCode()
    {
        return 127 * "value".hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        return this == rhs || ( rhs instanceof Description && value.equals( ( (Description) rhs ).value() ) );
    }

    @Override
    public String toString()
    {
        return null != source ? source.toString() : "@" + Description.class.getName() + "(value=" + value + ")";
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Annotation> T getAnnotation( final Binding<?> binding, final Class<T> annotationType )
    {
        if ( Description.class.equals( annotationType ) )
        {
            return (T) this;
        }
        if ( source instanceof AnnotatedSource )
        {
            return ( (AnnotatedSource) source ).getAnnotation( binding, annotationType );
        }
        return null;
    }
}
