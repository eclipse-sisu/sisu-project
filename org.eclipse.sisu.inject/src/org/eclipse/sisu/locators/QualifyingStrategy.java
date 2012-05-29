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
package org.eclipse.sisu.locators;

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Enumerates the different strategies for qualifying {@link Binding}s against requirement {@link Key}s.
 */
enum QualifyingStrategy
{
    // ----------------------------------------------------------------------
    // Enumerated values
    // ----------------------------------------------------------------------

    UNRESTRICTED
    {
        @Override
        final Annotation qualifies( final Key<?> requirement, final Binding<?> binding )
        {
            final Annotation qualifier = qualify( binding.getKey() );
            return null != qualifier ? qualifier : BLANK_QUALIFIER;
        }
    },
    NAMED
    {
        @Override
        final Annotation qualifies( final Key<?> requirement, final Binding<?> binding )
        {
            final Annotation qualifier = qualify( binding.getKey() );
            return qualifier instanceof Named ? qualifier : null;
        }
    },
    NAMED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation qualifies( final Key<?> requirement, final Binding<?> binding )
        {
            final Annotation qualifier = qualify( binding.getKey() );
            return requirement.getAnnotation().equals( qualifier ) ? qualifier : null;
        }
    },
    MARKED
    {
        @Override
        final Annotation qualifies( final Key<?> requirement, final Binding<?> binding )
        {
            final Class<? extends Annotation> markerType = requirement.getAnnotationType();

            final Annotation qualifier = qualify( binding.getKey() );
            if ( markerType.isInstance( qualifier ) )
            {
                return qualifier;
            }

            final Class<?> implementation = binding.acceptTargetVisitor( ImplementationVisitor.THIS );
            return null != implementation ? implementation.getAnnotation( markerType ) : null;
        }
    },
    MARKED_WITH_ATTRIBUTES
    {
        @Override
        final Annotation qualifies( final Key<?> requirement, final Binding<?> binding )
        {
            final Annotation qualifier = MARKED.qualifies( requirement, binding );
            return requirement.getAnnotation().equals( qualifier ) ? qualifier : null;
        }
    };

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Annotation DEFAULT_QUALIFIER = Names.named( "default" );

    static final Annotation BLANK_QUALIFIER = Names.named( "" );

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to qualify the given {@link Binding} against the requirement {@link Key}.
     * 
     * @param requirement The requirement key
     * @param binding The binding to qualify
     * @return Qualifier annotation when the binding qualifies; otherwise {@code null}
     */
    abstract Annotation qualifies( final Key<?> requirement, final Binding<?> binding );

    /**
     * Selects the appropriate qualifying strategy for the given requirement {@link Key}.
     * 
     * @param key The requirement key
     * @return Qualifying strategy
     */
    static final QualifyingStrategy selectFor( final Key<?> key )
    {
        final Class<?> qualifierType = key.getAnnotationType();
        if ( null == qualifierType )
        {
            return QualifyingStrategy.UNRESTRICTED;
        }
        if ( Named.class == qualifierType )
        {
            return key.hasAttributes() ? QualifyingStrategy.NAMED_WITH_ATTRIBUTES : QualifyingStrategy.NAMED;
        }
        return key.hasAttributes() ? QualifyingStrategy.MARKED_WITH_ATTRIBUTES : QualifyingStrategy.MARKED;
    }

    /**
     * Computes a canonical {@link Qualifier} annotation for the given binding {@link Key}.
     * 
     * @param key The key to qualify
     * @return Qualifier for the key
     */
    static final Annotation qualify( final Key<?> key )
    {
        if ( key instanceof WildcardKey )
        {
            final Annotation qualifier = ( (WildcardKey) key ).getQualifier();
            return null != qualifier ? qualifier : DEFAULT_QUALIFIER;
        }
        return null != key.getAnnotationType() ? key.getAnnotation() : DEFAULT_QUALIFIER;
    }
}
