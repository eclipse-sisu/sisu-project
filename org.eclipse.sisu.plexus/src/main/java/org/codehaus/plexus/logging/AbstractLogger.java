/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.logging;

public abstract class AbstractLogger
    implements Logger
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String name;

    private int threshold;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public AbstractLogger( final int threshold, final String name )
    {
        this.name = name;
        if ( threshold < LEVEL_DEBUG || LEVEL_DISABLED < threshold )
        {
            throw new IllegalArgumentException( "Threshold " + threshold + " is not valid" );
        }
        this.threshold = threshold;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void debug( final String message )
    {
        debug( message, null );
    }

    public boolean isDebugEnabled()
    {
        return threshold <= LEVEL_DEBUG;
    }

    public final void info( final String message )
    {
        info( message, null );
    }

    public boolean isInfoEnabled()
    {
        return threshold <= LEVEL_INFO;
    }

    public final void warn( final String message )
    {
        warn( message, null );
    }

    public boolean isWarnEnabled()
    {
        return threshold <= LEVEL_WARN;
    }

    public final void error( final String message )
    {
        error( message, null );
    }

    public boolean isErrorEnabled()
    {
        return threshold <= LEVEL_ERROR;
    }

    public final void fatalError( final String message )
    {
        fatalError( message, null );
    }

    public boolean isFatalErrorEnabled()
    {
        return threshold <= LEVEL_FATAL;
    }

    public final int getThreshold()
    {
        return threshold;
    }

    public final void setThreshold( final int threshold )
    {
        this.threshold = threshold;
    }

    public final String getName()
    {
        return name;
    }
}
