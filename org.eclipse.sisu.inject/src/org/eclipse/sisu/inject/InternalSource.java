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

import org.eclipse.sisu.Internal;

/**
 * Implementation of @{@link Internal} that can also act as an @{@link AnnotatedSource}.
 */
final class InternalSource
    implements Internal, AnnotatedSource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Object source;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * @param source The owning source
     */
    InternalSource( final Object source )
    {
        this.source = source;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<? extends Annotation> annotationType()
    {
        return Internal.class;
    }

    @Override
    public int hashCode()
    {
        return 0;
    }

    @Override
    public boolean equals( final Object rhs )
    {
        return rhs instanceof Internal;
    }

    @Override
    public String toString()
    {
        return null != source ? source.toString() : "@" + Internal.class.getName();
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Annotation> T getAnnotation( final Class<T> clazz )
    {
        if ( Internal.class.equals( clazz ) )
        {
            return (T) this;
        }
        if ( source instanceof AnnotatedSource )
        {
            return ( (AnnotatedSource) source ).getAnnotation( clazz );
        }
        return null;
    }
}
