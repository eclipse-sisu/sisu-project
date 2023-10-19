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
package org.eclipse.sisu.launch;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Properties;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.eclipse.sisu.inject.BeanLocator;

/**
 * Still JUnit3 based test
 * Execute with JUnit3 runner.
 */
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
        binder.install( new FactoryModuleBuilder().implement( Foo.class,
                                                              AssistedFoo.class ).build( FooFactory.class ) );
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
