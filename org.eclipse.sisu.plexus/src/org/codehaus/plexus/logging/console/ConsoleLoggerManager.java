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
package org.codehaus.plexus.logging.console;

import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLoggerManager
    extends AbstractLoggerManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Logger logger = new ConsoleLogger();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setThreshold( final String threshold )
    {
        logger.setThreshold( BaseLoggerManager.parseThreshold( threshold ) );
    }

    public Logger getLoggerForComponent( final String role, final String hint )
    {
        return logger;
    }

    public void returnComponentLogger( final String role, final String hint )
    {
        // nothing to do
    }

    public int getThreshold()
    {
        return logger.getThreshold();
    }

    public void setThreshold( final int currentThreshold )
    {
        logger.setThreshold( currentThreshold );
    }

    public void setThresholds( final int currentThreshold )
    {
        logger.setThreshold( currentThreshold );
    }

    public int getActiveLoggerCount()
    {
        return 0;
    }
}
