/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import org.eclipse.sisu.inject.DeferredClass;

public class BrokenScanningExample {
    public BrokenScanningExample(boolean strict) throws MalformedURLException {
        final ClassSpace space = new URLClassSpace(
                getClass().getClassLoader(), new URL[] {getClass().getResource("")});

        final URL badURL = new URL("oops:bad/");
        final ClassSpace brokenResourceSpace = new ClassSpace() {
            @Override
            public Class<?> loadClass(final String name) {
                return space.loadClass(name);
            }

            @Override
            public DeferredClass<?> deferLoadClass(final String name) {
                return space.deferLoadClass(name);
            }

            @Override
            public Enumeration<URL> getResources(final String name) {
                return space.getResources(name);
            }

            @Override
            public URL getResource(final String name) {
                return badURL;
            }

            @Override
            public Enumeration<URL> findEntries(final String path, final String glob, final boolean recurse) {
                return space.findEntries(path, glob, recurse);
            }
        };

        new SpaceScanner(brokenResourceSpace, strict).accept(new QualifiedTypeVisitor(null));
    }
}
