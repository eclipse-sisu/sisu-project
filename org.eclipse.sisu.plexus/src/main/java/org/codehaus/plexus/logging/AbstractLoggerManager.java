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

import org.eclipse.sisu.bean.IgnoreSetters;
import org.eclipse.sisu.plexus.Hints;

@IgnoreSetters
public abstract class AbstractLoggerManager
    implements LoggerManager
{
    public final Logger getLoggerForComponent( final String role )
    {
        return getLoggerForComponent( role, Hints.DEFAULT_HINT );
    }

    public final void returnComponentLogger( final String role )
    {
        returnComponentLogger( role, Hints.DEFAULT_HINT );
    }
}
