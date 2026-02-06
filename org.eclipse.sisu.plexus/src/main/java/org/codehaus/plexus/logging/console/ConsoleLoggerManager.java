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

import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLoggerManager extends AbstractLoggerManager {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Logger logger = new ConsoleLogger();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setThreshold(final String threshold) {
        logger.setThreshold(BaseLoggerManager.parseThreshold(threshold));
    }

    @Override
    public Logger getLoggerForComponent(final String role, final String hint) {
        return logger;
    }

    @Override
    public void returnComponentLogger(final String role, final String hint) {
        // nothing to do
    }

    @Override
    public int getThreshold() {
        return logger.getThreshold();
    }

    @Override
    public void setThreshold(final int currentThreshold) {
        logger.setThreshold(currentThreshold);
    }

    @Override
    public void setThresholds(final int currentThreshold) {
        logger.setThreshold(currentThreshold);
    }

    @Override
    public int getActiveLoggerCount() {
        return 0;
    }
}
