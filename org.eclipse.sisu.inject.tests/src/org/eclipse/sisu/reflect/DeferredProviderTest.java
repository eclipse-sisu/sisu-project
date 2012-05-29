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

import javax.inject.Inject;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public class DeferredProviderTest
    extends TestCase
{
    interface A
    {
    }

    interface B
    {
    }

    interface C
    {
    }

    static class AImpl
        implements A
    {
    }

    static class BImpl
        implements B
    {
        @Inject
        A a;
    }

    static class CImpl
        implements C
    {
        @Inject
        B b;
    }

    public void testRootDeferredProvider()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                bind( B.class ).to( BImpl.class );
                bind( A.class ).to( AImpl.class );
            }
        } ).getInstance( C.class );
    }

    public void testChildDeferredProvider()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( A.class ).to( AImpl.class );
            }
        } ).createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( B.class ).to( BImpl.class );
            }
        } ).createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
            }
        } ).getInstance( C.class );
    }

    public void testBrokenDeferredProvider()
    {
        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new ProvisionException( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new LinkageError( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected LinkageError" );
        }
        catch ( final LinkageError e )
        {
        }

        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new LoadedClass<C>( CImpl.class ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new IllegalArgumentException( new IllegalStateException( new ThreadDeath() ) );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ThreadDeath" );
        }
        catch ( final ThreadDeath e )
        {
        }

        final ClassSpace space = new URLClassSpace( C.class.getClassLoader(), null );
        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( C.class ).toProvider( new NamedClass<C>( space, CImpl.class.getName() ).asProvider() );
                    bind( CImpl.class ).toProvider( new Provider<CImpl>()
                    {
                        public CImpl get()
                        {
                            throw new ProvisionException( "Broken Provider" );
                        }
                    } );
                }
            } ).getInstance( C.class );

            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }
    }

    public void testDeferredImplementationClass()
    {
        final ClassSpace space = new URLClassSpace( C.class.getClassLoader(), null );

        final DeferredClass<C> clazz1 = new NamedClass<C>( space, CImpl.class.getName() );
        final DeferredClass<C> clazz2 = new LoadedClass<C>( CImpl.class );

        final DeferredProvider<C> provider1 = clazz1.asProvider();
        final DeferredProvider<C> provider2 = clazz2.asProvider();

        assertSame( clazz1, provider1.getImplementationClass() );
        assertSame( clazz2, provider2.getImplementationClass() );

        assertTrue( provider1.toString().contains( clazz1.toString() ) );
        assertTrue( provider2.toString().contains( clazz2.toString() ) );
    }
}
