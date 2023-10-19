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

import java.net.URL;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.PrivateModule;

import org.junit.jupiter.api.Test;

class CachingModuleTest
{
    @Named
    static class CustomModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            requireBinding( CachingModuleTest.class );
            getMembersInjector( CachingModuleTest.class );

            install( new PrivateModule()
            {
                @Override
                protected void configure()
                {
                    requireBinding( CachingModuleTest.class );
                    getMembersInjector( CachingModuleTest.class );
                }
            } );
        }
    }

    @Test
    void testQualifiedModule()
    {
        final ClassSpace space =
            new URLClassSpace( getClass().getClassLoader(), new URL[] { getClass().getResource( "" ) } );

        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
        Guice.createInjector( new SpaceModule( space, BeanScanning.CACHE ) );
    }
}
