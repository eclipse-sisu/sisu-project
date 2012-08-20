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

import org.eclipse.sisu.plexus.config.Hints;

public interface PlexusConstants
{
    String PLEXUS_DEFAULT_HINT = Hints.DEFAULT_HINT;

    String PLEXUS_KEY = "plexus";

    String GLOBAL_VISIBILITY = "global";

    String REALM_VISIBILITY = "realm";

    String SCANNING_ON = "on";

    String SCANNING_OFF = "off";

    String SCANNING_INDEX = "index";

    String SCANNING_CACHE = "cache";
}
