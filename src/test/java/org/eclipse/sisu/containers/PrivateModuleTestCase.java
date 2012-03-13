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

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;

public final class PrivateModuleTestCase
    extends InjectedTestCase
{
    static class PrivateFoo
        implements Foo
    {
        final String host;

        final int port;

        @Inject
        public PrivateFoo( @Named( "${host}" ) final String host, @Named( "port" ) final int port )
        {
            this.host = host;
            this.port = port;
        }
    }

    @Override
    public void configure( final Binder binder )
    {
        binder.install( new PrivateModule()
        {
            @Override
            protected void configure()
            {
                bind( Foo.class ).to( PrivateFoo.class );
                bindConstant().annotatedWith( Names.named( "port" ) ).to( 8081 );
                expose( Foo.class );
            }
        } );

        binder.install( new PrivateModule()
        {
            @Override
            protected void configure()
            {
                bind( Foo.class ).annotatedWith( Names.named( "A" ) ).to( PrivateFoo.class );
                bindConstant().annotatedWith( Names.named( "port" ) ).to( 1234 );
                expose( Foo.class ).annotatedWith( Names.named( "A" ) );
            }
        } );

        binder.install( new PrivateModule()
        {
            @Override
            protected void configure()
            {
                bind( Foo.class ).annotatedWith( Names.named( "B" ) ).to( PrivateFoo.class );
                bindConstant().annotatedWith( Names.named( "port" ) ).to( 4321 );
                expose( Foo.class ).annotatedWith( Names.named( "B" ) );
            }
        } );
    }

    @Override
    public void configure( final Properties properties )
    {
        properties.setProperty( "host", "127.0.0.1" );
    }

    @Inject
    Foo bean;

    @Inject
    @Named( "A" )
    Foo beanA;

    @Inject
    @Named( "B" )
    Foo beanB;

    public void testAssistedInject()
    {
        assertTrue( bean instanceof PrivateFoo );
        assertEquals( "127.0.0.1", ( (PrivateFoo) bean ).host );
        assertEquals( 8081, ( (PrivateFoo) bean ).port );

        assertTrue( beanA instanceof PrivateFoo );
        assertEquals( "127.0.0.1", ( (PrivateFoo) beanA ).host );
        assertEquals( 1234, ( (PrivateFoo) beanA ).port );

        assertTrue( beanB instanceof PrivateFoo );
        assertEquals( "127.0.0.1", ( (PrivateFoo) beanB ).host );
        assertEquals( 4321, ( (PrivateFoo) beanB ).port );
    }
}
