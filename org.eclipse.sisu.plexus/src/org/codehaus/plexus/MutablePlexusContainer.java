/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.codehaus.plexus;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public interface MutablePlexusContainer
    extends PlexusContainer
{
    LoggerManager getLoggerManager();

    void setLoggerManager( LoggerManager loggerManager );

    Logger getLogger();

    ClassWorld getClassWorld();
}
