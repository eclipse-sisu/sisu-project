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
package org.codehaus.plexus.logging;

public abstract class AbstractLogEnabled implements LogEnabled {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Logger logger;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void enableLogging(final Logger theLogger) {
        logger = theLogger;
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected Logger getLogger() {
        return logger;
    }

    protected final void setupLogger(final Object component) {
        setupLogger(component, logger);
    }

    protected final void setupLogger(final Object component, final String category) {
        if (category == null) {
            throw new IllegalStateException("Logging category must be defined.");
        }
        setupLogger(component, logger.getChildLogger(category));
    }

    @SuppressWarnings("static-method")
    protected final void setupLogger(final Object component, final Logger logger) {
        if (component instanceof LogEnabled) {
            ((LogEnabled) component).enableLogging(logger);
        }
    }
}
