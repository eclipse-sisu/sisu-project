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
package org.codehaus.plexus;

import org.eclipse.sisu.plexus.Hints;

public interface PlexusConstants {
    String PLEXUS_DEFAULT_HINT = Hints.DEFAULT_HINT;

    String PLEXUS_KEY = "plexus";

    String GLOBAL_VISIBILITY = "global";

    String REALM_VISIBILITY = "realm";

    String SCANNING_ON = "on";

    String SCANNING_OFF = "off";

    String SCANNING_INDEX = "index";

    String SCANNING_CACHE = "cache";
}
