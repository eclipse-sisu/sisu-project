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

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.eclipse.sisu.inject.Weak;
import org.eclipse.sisu.plexus.Roles;

public abstract class BaseLoggerManager
    extends AbstractLoggerManager
    implements Initializable
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Logger> activeLoggers = Weak.values();

    String threshold = "INFO";

    private int currentThreshold;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void initialize()
    {
        currentThreshold = parseThreshold( threshold );
    }

    public final synchronized Logger getLoggerForComponent( final String role, final String hint )
    {
        final String name = Roles.canonicalRoleHint( role, hint );
        Logger logger = activeLoggers.get( name );
        if ( null == logger )
        {
            logger = createLogger( name );
            logger.setThreshold( currentThreshold );
            activeLoggers.put( name, logger );
        }
        return logger;
    }

    public final synchronized void returnComponentLogger( final String role, final String hint )
    {
        activeLoggers.remove( Roles.canonicalRoleHint( role, hint ) );
    }

    public final int getThreshold()
    {
        return currentThreshold;
    }

    public final void setThreshold( final int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
    }

    public final synchronized void setThresholds( final int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
        for ( final Logger logger : activeLoggers.values() )
        {
            logger.setThreshold( currentThreshold );
        }
    }

    public static final int parseThreshold( final String text )
    {
        if ( "DEBUG".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_DEBUG;
        }
        else if ( "INFO".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_INFO;
        }
        else if ( "WARN".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_WARN;
        }
        else if ( "ERROR".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_ERROR;
        }
        else if ( "FATAL".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_FATAL;
        }
        else if ( "DISABLED".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_DISABLED;
        }
        return Logger.LEVEL_DEBUG;
    }

    public final synchronized int getActiveLoggerCount()
    {
        return activeLoggers.size();
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract Logger createLogger( String name );
}
