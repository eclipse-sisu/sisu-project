/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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

public final class PlexusContainerException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public PlexusContainerException( final String message )
    {
        super( message );
    }

    public PlexusContainerException( final String message, final Throwable detail )
    {
        super( message, detail );
    }
}
