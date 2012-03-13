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
package org.eclipse.sisu.containers;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

public final class Custom3TestCase
    extends InjectedTestCase
{
    @Override
    public void configure( final Properties properties )
    {
        properties.put( "hint", "NameTag" );
        properties.put( "port", "8080" );
    }

    @Inject
    @Named( "${hint}" )
    Foo bean;

    @Inject
    @Named( "${port}" )
    int port;

    public void testPerTestCaseCustomization()
    {
        assertTrue( bean instanceof NamedAndTaggedFoo );

        assertEquals( 8080, port );
    }
}
