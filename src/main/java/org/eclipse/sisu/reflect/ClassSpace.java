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
package org.eclipse.sisu.reflect;

import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * Represents an abstract collection of related classes and resources.
 */
public interface ClassSpace
{
    /**
     * Loads the named class from the surrounding class space.
     * 
     * @param name The class name
     * @return Class instance
     * @see ClassLoader#loadClass(String)
     */
    Class<?> loadClass( String name )
        throws TypeNotPresentException;

    /**
     * Defers loading of the named class from the surrounding class space.
     * 
     * @param name The class name
     * @return Deferred class
     * @see ClassLoader#loadClass(String)
     */
    DeferredClass<?> deferLoadClass( String name );

    /**
     * Queries the surrounding class space for the resource with the given name.
     * 
     * @param name The resource name
     * @return URL pointing to the resource; {@code null} if it wasn't found
     * @see ClassLoader#getResource(String)
     */
    URL getResource( String name );

    /**
     * Queries the surrounding class space for all resources with the given name.
     * 
     * @param name The resource name
     * @return Sequence of URLs, one for each matching resource
     * @see ClassLoader#getResources(String)
     */
    Enumeration<URL> getResources( String name );

    /**
     * Queries local class space content for entries matching the given pattern.
     * 
     * @param path The initial search directory; for example {@code "META-INF"}
     * @param glob The filename glob pattern; for example {@code "*.xml"}
     * @param recurse If {@code true} recurse into sub-directories; otherwise only search initial directory
     * @return Sequence of URLs, one for each matching entry
     * @see Bundle#findEntries(String, String, boolean)
     */
    Enumeration<URL> findEntries( String path, String glob, boolean recurse );
}
