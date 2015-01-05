/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.net.URL;
import java.util.Enumeration;

/**
 * {@link ClassFinder} that finds {@link Class} resources under a given package name.
 */
public final class DefaultClassFinder
    implements ClassFinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String path;

    private final boolean recurse;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultClassFinder( final String pkg )
    {
        final String tempPath = pkg.replace( '.', '/' );
        if ( tempPath.endsWith( "*" ) )
        {
            path = tempPath.substring( 0, tempPath.length() - 1 );
            recurse = true;
        }
        else
        {
            path = tempPath;
            recurse = false;
        }
    }

    public DefaultClassFinder()
    {
        path = null;
        recurse = true;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Enumeration<URL> findClasses( final ClassSpace space )
    {
        return space.findEntries( path, "*.class", recurse );
    }
}
