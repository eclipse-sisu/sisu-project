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
package org.eclipse.sisu.space;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Binding {@link Key} for implementations that act as "wild-cards", meaning they match against any assignable type.
 * <p>
 * Since the wild-card type is {@link Object} and the associated qualifier may not be unique between implementations,
 * the qualifier is saved and replaced with a unique (per-implementation) pseudo-qualifier. The original qualifier is
 * available by casting the pseudo-qualifier to {@link Provider} and calling {@code get()}.
 */
final class WildcardKey
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<Object> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private WildcardKey()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * @return Wildcard key for the given implementation type and qualifier
     */
    public static Key<Object> get( final Class<?> type, final Annotation qualifier )
    {
        return Key.get( OBJECT_TYPE_LITERAL, new QualifiedImpl( type, qualifier ) );
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
        implements Qualified, Provider<Annotation>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Class<?> value;

        private final Annotation qualifier;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        QualifiedImpl( final Class<?> value, final Annotation qualifier )
        {
            this.value = value;
            this.qualifier = qualifier;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Class<?> value()
        {
            return value;
        }

        public Annotation get()
        {
            return qualifier;
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
