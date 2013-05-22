/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

/**
 * {@link ClassFinder} that uses the qualified class index to select implementations to scan.
 */
public final class SisuIndexClassFinder
    extends IndexedClassFinder
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SisuIndexClassFinder( final boolean globalIndex )
    {
        super( "META-INF/sisu/" + SisuIndex.NAMED, globalIndex );
    }
}
