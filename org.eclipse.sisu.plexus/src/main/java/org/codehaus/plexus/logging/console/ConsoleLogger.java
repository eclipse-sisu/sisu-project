/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.codehaus.plexus.logging.console;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLogger extends AbstractLogger {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] TAGS = {"[DEBUG] ", "[INFO] ", "[WARNING] ", "[ERROR] ", "[FATAL ERROR] "};

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ConsoleLogger(final int threshold, final String name) {
        super(threshold, name);
    }

    public ConsoleLogger() {
        this(Logger.LEVEL_INFO, "console");
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void debug(final String message, final Throwable throwable) {
        if (isDebugEnabled()) {
            log(LEVEL_DEBUG, message, throwable);
        }
    }

    @Override
    public void info(final String message, final Throwable throwable) {
        if (isInfoEnabled()) {
            log(LEVEL_INFO, message, throwable);
        }
    }

    @Override
    public void warn(final String message, final Throwable throwable) {
        if (isWarnEnabled()) {
            log(LEVEL_WARN, message, throwable);
        }
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        if (isErrorEnabled()) {
            log(LEVEL_ERROR, message, throwable);
        }
    }

    @Override
    public void fatalError(final String message, final Throwable throwable) {
        if (isFatalErrorEnabled()) {
            log(LEVEL_FATAL, message, throwable);
        }
    }

    @Override
    public Logger getChildLogger(final String name) {
        return this;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static void log(final int level, final String message, final Throwable throwable) {
        System.out.println(TAGS[level].concat(message));
        if (throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }
}
