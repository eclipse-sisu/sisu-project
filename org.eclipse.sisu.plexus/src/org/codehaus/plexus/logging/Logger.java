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

public interface Logger
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    int LEVEL_DEBUG = 0;

    int LEVEL_INFO = 1;

    int LEVEL_WARN = 2;

    int LEVEL_ERROR = 3;

    int LEVEL_FATAL = 4;

    int LEVEL_DISABLED = 5;

    // ----------------------------------------------------------------------
    // Logging methods
    // ----------------------------------------------------------------------

    void debug( String message );

    void debug( String message, Throwable throwable );

    boolean isDebugEnabled();

    void info( String message );

    void info( String message, Throwable throwable );

    boolean isInfoEnabled();

    void warn( String message );

    void warn( String message, Throwable throwable );

    boolean isWarnEnabled();

    void error( String message );

    void error( String message, Throwable throwable );

    boolean isErrorEnabled();

    void fatalError( String message );

    void fatalError( String message, Throwable throwable );

    boolean isFatalErrorEnabled();

    // ----------------------------------------------------------------------
    // Management methods
    // ----------------------------------------------------------------------

    int getThreshold();

    void setThreshold( int threshold );

    Logger getChildLogger( String name );

    String getName();
}
