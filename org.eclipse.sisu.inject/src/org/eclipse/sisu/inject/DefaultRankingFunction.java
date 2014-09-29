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
package org.eclipse.sisu.inject;

import javax.inject.Inject;

import org.eclipse.sisu.Priority;

import com.google.inject.Binding;
import com.google.inject.spi.BindingTargetVisitor;

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
        ImplementationVisitor servletVisitor;
        try
        {
            servletVisitor = new ServletVisitor();
        }
        catch ( final LinkageError e )
        {
            servletVisitor = null;
        }
        SERVLET_VISITOR = servletVisitor;

        boolean hasJsr250Priority;
        try
        {
            hasJsr250Priority = javax.annotation.Priority.class.isAnnotation();
        }
        catch ( final LinkageError e )
        {
            hasJsr250Priority = false;
        }
        HAS_JSR250_PRIORITY = hasJsr250Priority;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final BindingTargetVisitor<Object, Class<?>> SERVLET_VISITOR;

    private static final BindingTargetVisitor<Object, Class<?>> TARGET_VISITOR =
        SERVLET_VISITOR == null ? ImplementationVisitor.THIS : SERVLET_VISITOR;

    private static final boolean HAS_JSR250_PRIORITY;

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
        final Object source = InjectorPublisher.getDeclaringSource( binding );
        if ( source instanceof PriorityBinding )
        {
            return ( (PriorityBinding) source ).getPriority();
        }
        final Class<?> implementation = binding.acceptTargetVisitor( TARGET_VISITOR );
        if ( null != implementation )
        {
            if ( HAS_JSR250_PRIORITY )
            {
                final javax.annotation.Priority priority =
                    implementation.getAnnotation( javax.annotation.Priority.class );
                if ( null != priority )
                {
                    return priority.value();
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
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ImplementationVisitor} that can also peek behind filter/servlet bindings.
     */
    static final class ServletVisitor
        extends ImplementationVisitor
        implements com.google.inject.servlet.ServletModuleTargetVisitor<Object, Class<?>>
    {
        public Class<?> visit( final com.google.inject.servlet.InstanceFilterBinding binding )
        {
            return binding.getFilterInstance().getClass();
        }

        public Class<?> visit( final com.google.inject.servlet.InstanceServletBinding binding )
        {
            return binding.getServletInstance().getClass();
        }

        public Class<?> visit( final com.google.inject.servlet.LinkedFilterBinding binding )
        {
            // this assumes only one level of indirection: api-->impl
            return binding.getLinkedKey().getTypeLiteral().getRawType();
        }

        public Class<?> visit( final com.google.inject.servlet.LinkedServletBinding binding )
        {
            // this assumes only one level of indirection: api-->impl
            return binding.getLinkedKey().getTypeLiteral().getRawType();
        }
    }
}
