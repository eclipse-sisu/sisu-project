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
package org.eclipse.sisu.plexus.scanners;

import org.eclipse.sisu.plexus.scanners.PlexusXmlScanner;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.URLClassSpace;

public class SimpleScanningExample
{
    public SimpleScanningExample()
    {
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        new PlexusXmlScanner( null, null, null ).scan( space, true );
    }
}
