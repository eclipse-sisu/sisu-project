/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
package org.eclipse.sisu.space;

import java.net.URL;
import java.util.Enumeration;

import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BaseTests
class DefaultClassFinderTest
{
    @Test
    void testDefaultConstructor()
    {
        final DefaultClassFinder finder = new DefaultClassFinder();
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final Enumeration<URL> classes = finder.findClasses( space );
        assertNotNull( classes );
        assertTrue( classes.hasMoreElements() );
    }

    @Test
    void testPackageConstructorNonRecursive()
    {
        final DefaultClassFinder finder = new DefaultClassFinder( "org.eclipse.sisu.space" );
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final Enumeration<URL> classes = finder.findClasses( space );
        assertNotNull( classes );
    }

    @Test
    void testPackageConstructorRecursive()
    {
        final DefaultClassFinder finder = new DefaultClassFinder( "org.eclipse.sisu.space.*" );
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final Enumeration<URL> classes = finder.findClasses( space );
        assertNotNull( classes );
    }

    @Test
    void testEmptyPackage()
    {
        final DefaultClassFinder finder = new DefaultClassFinder( "org.eclipse.sisu.nonexistent.*" );
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader() );
        final Enumeration<URL> classes = finder.findClasses( space );
        assertNotNull( classes );
    }
}
