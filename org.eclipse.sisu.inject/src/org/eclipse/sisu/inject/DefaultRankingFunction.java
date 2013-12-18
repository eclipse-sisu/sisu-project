/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
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
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.eclipse.sisu.Priority;

import com.google.inject.Binding;

/**
 * Simple {@link RankingFunction} that partitions qualified bindings into two main groups.
 * <p>
 * Default bindings are given zero or positive ranks; the rest are given negative ranks.
 */
public final class DefaultRankingFunction
    implements RankingFunction
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        Method jsr250PriorityValue;
        try
        {
            final Class<?> clazz = Priority.class.getClassLoader().loadClass( "javax.annotation.Priority" );
            jsr250PriorityValue = clazz.getMethod( "value" );
        }
        catch ( final Exception e )
        {
            jsr250PriorityValue = null;
        }
        catch ( final LinkageError e )
        {
            jsr250PriorityValue = null;
        }
        JSR250_PRIORITY_VALUE = jsr250PriorityValue;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Method JSR250_PRIORITY_VALUE;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final int primaryRank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultRankingFunction( final int primaryRank )
    {
        if ( primaryRank < 0 )
        {
            throw new IllegalArgumentException( "Primary rank must be zero or more" );
        }
        this.primaryRank = primaryRank;
    }

    @Inject
    public DefaultRankingFunction()
    {
        this( 0 ); // use this as the default primary rank unless otherwise configured
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public int maxRank()
    {
        return primaryRank;
    }

    public <T> int rank( final Binding<T> binding )
    {
        final Class<?> implementation = binding.acceptTargetVisitor( ImplementationVisitor.THIS );
        if ( null != implementation )
        {
            if ( null != JSR250_PRIORITY_VALUE )
            {
                final Object value = getJSR250PriorityValue( implementation );
                if ( value instanceof Number )
                {
                    return ( (Number) value ).intValue();
                }
            }
            final Priority priority = implementation.getAnnotation( Priority.class );
            if ( null != priority )
            {
                return priority.value();
            }
        }
        if ( QualifyingStrategy.DEFAULT_QUALIFIER.equals( QualifyingStrategy.qualify( binding.getKey() ) ) )
        {
            return primaryRank;
        }
        return primaryRank + Integer.MIN_VALUE; // shifts primary range of [0,MAX_VALUE] down to [MIN_VALUE,-1]
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Object getJSR250PriorityValue( final Class<?> clazz )
    {
        @SuppressWarnings( "unchecked" )
        final Object priority = clazz.getAnnotation( (Class<Annotation>) JSR250_PRIORITY_VALUE.getDeclaringClass() );
        if ( null != priority )
        {
            try
            {
                return JSR250_PRIORITY_VALUE.invoke( priority );
            }
            catch ( final Exception e )
            {
                // ignore
            }
            catch ( final LinkageError e )
            {
                // ignore
            }
        }
        return null;
    }
}
