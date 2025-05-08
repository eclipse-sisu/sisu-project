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

import org.eclipse.sisu.Priority;

import com.google.inject.Binding;

/**
 * Implementation of @{@link Priority} that can also act as an @{@link AnnotatedSource}.
 */
final class PrioritySource
    implements Priority, AnnotatedSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Object source;

    private final int value;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * @param source The owning source
     * @param value The priority
     */
    PrioritySource( final Object source, final int value )
    {
        this.source = source;
        this.value = value;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public int value()
    {
        return value;
    }

    public Class<? extends Annotation> annotationType()
    {
        return Priority.class;
    }

    @Override
    public int hashCode()
    {
        return 127 * "value".hashCode() ^ Integer.valueOf( value ).hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        return this == rhs || ( rhs instanceof Priority && value == ( (Priority) rhs ).value() );
    }

    @Override
    public String toString()
    {
        return null != source ? source.toString() : "@" + Priority.class.getName() + "(value=" + value + ")";
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Annotation> T getAnnotation( final Binding<?> binding, final Class<T> annotationType )
    {
        if ( Priority.class.equals( annotationType ) )
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
