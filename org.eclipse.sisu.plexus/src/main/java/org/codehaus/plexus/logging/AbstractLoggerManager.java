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

import org.eclipse.sisu.bean.IgnoreSetters;
import org.eclipse.sisu.plexus.Hints;

@IgnoreSetters
public abstract class AbstractLoggerManager implements LoggerManager {
    @Override
    public final Logger getLoggerForComponent(final String role) {
        return getLoggerForComponent(role, Hints.DEFAULT_HINT);
    }

    @Override
    public final void returnComponentLogger(final String role) {
        returnComponentLogger(role, Hints.DEFAULT_HINT);
    }
}
