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

import javax.inject.Inject;

import org.eclipse.sisu.Priority;

import com.google.inject.Binding;
import com.google.inject.Singleton;

/**
 * Simple {@link RankingFunction} that partitions qualified bindings into two main groups.
 * <p>
 * Default bindings are given zero or positive ranks; the rest are given negative ranks.
 */
@Singleton
public final class DefaultRankingFunction
    implements RankingFunction
{
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
        return Integer.MAX_VALUE; // since bindings can use @Priority to escalate their rank to MAX_VALUE
    }

    public <T> int rank( final Binding<T> binding )
    {
        final Priority priority = Sources.getAnnotation( binding, Priority.class );
        if ( null != priority )
        {
            return priority.value();
        }
        if ( QualifyingStrategy.DEFAULT_QUALIFIER.equals( QualifyingStrategy.qualify( binding.getKey() ) ) )
        {
            return primaryRank;
        }
        return primaryRank + Integer.MIN_VALUE; // shifts primary range of [0,MAX_VALUE] down to [MIN_VALUE,-1]
    }
}
