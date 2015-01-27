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

import org.eclipse.sisu.Hidden;

import com.google.inject.Binding;

/**
 * Implementation of @{@link Hidden} that can also act as an @{@link AnnotatedSource}.
 */
final class HiddenSource
    implements Hidden, AnnotatedSource
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
    HiddenSource( final Object source )
    {
        this.source = source;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<? extends Annotation> annotationType()
    {
        return Hidden.class;
    }

    @Override
    public int hashCode()
    {
        return 0;
    }

    @Override
    public boolean equals( final Object rhs )
    {
        return rhs instanceof Hidden;
    }

    @Override
    public String toString()
    {
        return null != source ? source.toString() : "@" + Hidden.class.getName();
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Annotation> T getAnnotation( final Binding<?> binding, final Class<T> annotationType )
    {
        if ( Hidden.class.equals( annotationType ) )
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
