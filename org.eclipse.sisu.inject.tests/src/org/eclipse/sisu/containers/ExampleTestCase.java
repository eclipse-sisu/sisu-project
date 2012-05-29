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

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.name.Names;

public final class ExampleTestCase
    extends InjectedTestCase
{
    @Inject
    @Named( "${basedir}" )
    String basedir;

    @Inject
    @Named( "${basedir}/target/test-classes/org/eclipse/sisu/containers/inject.properties" )
    File propertiesFile;

    public void testBasedir()
    {
        assertEquals( getBasedir(), basedir );
        assertTrue( propertiesFile.isFile() );
    }

    @Inject
    Foo bean;

    @Inject
    Map<String, Foo> beans;

    public void testInjection()
    {
        assertTrue( bean instanceof DefaultFoo );

        assertEquals( 4, beans.size() );

        assertTrue( beans.get( "default" ) instanceof DefaultFoo );
        assertTrue( beans.get( NamedFoo.class.getName() ) instanceof NamedFoo );
        assertTrue( beans.get( TaggedFoo.class.getName() ) instanceof TaggedFoo );
        assertTrue( beans.get( "NameTag" ) instanceof NamedAndTaggedFoo );

        assertTrue( bean == beans.get( "default" ) );
    }

    public void testContainerLookup()
    {
        assertTrue( lookup( Foo.class ) instanceof DefaultFoo );
        assertTrue( lookup( Foo.class, Named.class ) instanceof DefaultFoo );
        assertTrue( lookup( Foo.class, "NameTag" ) instanceof NamedAndTaggedFoo );
        assertTrue( lookup( Foo.class, Names.named( "NameTag" ) ) instanceof NamedAndTaggedFoo );
        assertTrue( lookup( Foo.class, Tag.class ).getClass().isAnnotationPresent( Tag.class ) );
        assertTrue( lookup( Foo.class, new TagImpl( "A" ) ) instanceof TaggedFoo );
        assertNull( lookup( Foo.class, new TagImpl( "X" ) ) );
        assertNull( lookup( Integer.class ) );
    }
}
