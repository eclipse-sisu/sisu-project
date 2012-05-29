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
package org.eclipse.sisu.scanners;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.DeferredClass;
import org.eclipse.sisu.reflect.URLClassSpace;

public class BrokenScanningExample
{
    public BrokenScanningExample()
        throws MalformedURLException
    {
        final ClassSpace space =
            new URLClassSpace( getClass().getClassLoader(), new URL[] { getClass().getResource( "" ) } );

        final URL badURL = new URL( "oops:bad/" );
        final ClassSpace brokenResourceSpace = new ClassSpace()
        {
            public Class<?> loadClass( final String name )
            {
                return space.loadClass( name );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return space.deferLoadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
            {
                return space.getResources( name );
            }

            public URL getResource( final String name )
            {
                return badURL;
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
            {
                return space.findEntries( path, glob, recurse );
            }
        };

        new ClassSpaceScanner( brokenResourceSpace ).accept( new QualifiedTypeVisitor( null ) );
    }
}
