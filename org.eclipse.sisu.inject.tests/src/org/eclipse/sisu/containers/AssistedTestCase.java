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

import org.eclipse.sisu.locators.BeanLocator;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public final class AssistedTestCase
    extends InjectedTestCase
{
    interface FooFactory
    {
        Foo create( int port );
    }

    @Named
    static class AssistedFoo
        implements Foo
    {
        final int port;

        final String host;

        @Inject
        public AssistedFoo( @Assisted final int port, @Named( "${host}" ) final String host )
        {
            this.port = port;
            this.host = host;
        }
    }

    @Override
    public void configure( final Binder binder )
    {
        binder.install( new FactoryModuleBuilder().implement( Foo.class, AssistedFoo.class ).build( FooFactory.class ) );
    }

    @Override
    public void configure( final Properties properties )
    {
        properties.setProperty( "host", "localhost" );
    }

    @Inject
    FooFactory beanFactory;

    @Inject
    BeanLocator beanLocator;

    public void testAssistedInject()
    {
        Foo bean = beanFactory.create( 8080 );
        assertTrue( bean instanceof AssistedFoo );

        assertEquals( 8080, ( (AssistedFoo) bean ).port );
        assertEquals( "localhost", ( (AssistedFoo) bean ).host );

        bean = beanLocator.locate( Key.get( FooFactory.class ) ).iterator().next().getValue().create( 42 );

        assertEquals( 42, ( (AssistedFoo) bean ).port );
        assertEquals( "localhost", ( (AssistedFoo) bean ).host );

        bean = beanLocator.locate( Key.get( Foo.class ) ).iterator().next().getValue();

        assertTrue( bean instanceof DefaultFoo );
    }
}
