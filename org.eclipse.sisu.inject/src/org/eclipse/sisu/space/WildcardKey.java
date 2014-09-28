/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.inject.Key;

/**
 * Binding {@link Key} for implementations that act as "wild-cards", meaning they match against any assignable type.
 * <p>
 * Since the wild-card type is {@link Object} and the associated qualifier may not be unique between implementations,
 * the qualifier is saved and replaced with a unique (per-implementation) pseudo-qualifier. The original qualifier is
 * available from {@link #get()}.
 */
final class WildcardKey
    extends Key<Object>
    implements Provider<Annotation>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Annotation qualifier;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    WildcardKey( final Class<?> type, final Annotation qualifier )
    {
        super( new QualifiedImpl( type ) );
        this.qualifier = qualifier;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Original qualifier associated with the implementation
     */
    public Annotation get()
    {
        return qualifier;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Qualifier} that captures a qualified implementation type.
     */
    @Qualifier
    @Retention( RetentionPolicy.RUNTIME )
    private static @interface Qualified
    {
        Class<?> value();
    }

    /**
     * Pseudo-{@link Annotation} that can wrap any implementation type as a {@link Qualifier}.
     */
    private static final class QualifiedImpl
        implements Qualified
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Class<?> value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        QualifiedImpl( final Class<?> value )
        {
            this.value = value;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Class<?> value()
        {
            return value;
        }

        public Class<? extends Annotation> annotationType()
        {
            return Qualified.class;
        }

        @Override
        public int hashCode()
        {
            return value.hashCode(); // no need to follow strict annotation spec
        }

        @Override
        public boolean equals( final Object rhs )
        {
            if ( this == rhs )
            {
                return true;
            }
            if ( rhs instanceof QualifiedImpl )
            {
                return value == ( (QualifiedImpl) rhs ).value;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return "*"; // let people know this is a "wild-card" qualifier
        }
    }
}
