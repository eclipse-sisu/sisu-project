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
package org.eclipse.sisu.binders;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.sisu.Parameters;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;

public class ParametersTest
    extends TestCase
{
    @Inject
    @Parameters
    String[] arguments;

    @Inject
    @Parameters
    Map<String, String> properties;

    public void testDefaultParameters()
    {
        Guice.createInjector( new WireModule( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ParametersTest.class );
            }
        } ) ).injectMembers( this );

        assertTrue( properties.isEmpty() );
        assertEquals( 0, arguments.length );
    }

    @SuppressWarnings( { "unchecked", "rawtypes", "unused" } )
    public void testCustomParameters()
    {
        Guice.createInjector( new WireModule( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ParametersTest.class );
            }

            @Provides
            @Parameters
            String[] arguments()
            {
                return new String[] { "Hello", "World" };
            }

            @Provides
            @Parameters
            Map<String, String> properties()
            {
                return (Map) System.getProperties();
            }
        } ) ).injectMembers( this );

        assertEquals( System.getProperties(), properties );
        assertTrue( Arrays.equals( new String[] { "Hello", "World" }, arguments ) );
    }
}
