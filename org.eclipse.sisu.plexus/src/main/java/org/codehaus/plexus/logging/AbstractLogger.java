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

public abstract class AbstractLogger implements Logger {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String name;

    private int threshold;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public AbstractLogger(final int threshold, final String name) {
        this.name = name;
        if (threshold < LEVEL_DEBUG || LEVEL_DISABLED < threshold) {
            throw new IllegalArgumentException("Threshold " + threshold + " is not valid");
        }
        this.threshold = threshold;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public final void debug(final String message) {
        debug(message, null);
    }

    @Override
    public boolean isDebugEnabled() {
        return threshold <= LEVEL_DEBUG;
    }

    @Override
    public final void info(final String message) {
        info(message, null);
    }

    @Override
    public boolean isInfoEnabled() {
        return threshold <= LEVEL_INFO;
    }

    @Override
    public final void warn(final String message) {
        warn(message, null);
    }

    @Override
    public boolean isWarnEnabled() {
        return threshold <= LEVEL_WARN;
    }

    @Override
    public final void error(final String message) {
        error(message, null);
    }

    @Override
    public boolean isErrorEnabled() {
        return threshold <= LEVEL_ERROR;
    }

    @Override
    public final void fatalError(final String message) {
        fatalError(message, null);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return threshold <= LEVEL_FATAL;
    }

    @Override
    public final int getThreshold() {
        return threshold;
    }

    @Override
    public final void setThreshold(final int threshold) {
        this.threshold = threshold;
    }

    @Override
    public final String getName() {
        return name;
    }
}
