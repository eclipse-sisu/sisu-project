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

public interface LoggerManager {
    String ROLE = LoggerManager.class.getName();

    Logger getLoggerForComponent(String role);

    Logger getLoggerForComponent(String role, String hint);

    void returnComponentLogger(String role);

    void returnComponentLogger(String role, String hint);

    int getThreshold();

    void setThreshold(int threshold);

    void setThresholds(int threshold);

    int getActiveLoggerCount();
}
