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
package org.eclipse.sisu.inject;

import java.util.Iterator;

import org.eclipse.sisu.inject.RankedBindingsTest.Bean;
import org.eclipse.sisu.inject.RankedBindingsTest.BeanImpl;
import org.eclipse.sisu.space.LoadedClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.UntargettedBinding;
import com.google.inject.util.Providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImplementationsTest
{
    Injector injector;

    @BeforeEach
    void setUp()
        throws Exception
    {
        injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "linked" ) ).to( BeanImpl.class );

                try
                {
                    bind( Bean.class ).annotatedWith( Names.named( "ctor" ) ).toConstructor( BeanImpl.class.getDeclaredConstructor() );
                }
                catch ( final NoSuchMethodException e )
                {
                }

                bind( Bean.class ).annotatedWith( Names.named( "instance" ) ).toInstance( new BeanImpl() );

                bind( Bean.class ).annotatedWith( Names.named( "deferred" ) ).toProvider( new LoadedClass<Bean>( BeanImpl.class ) );

                install( new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        bind( Bean.class ).annotatedWith( Names.named( "exposed" ) ).to( BeanImpl.class );
                        expose( Bean.class ).annotatedWith( Names.named( "exposed" ) );
                    }
                } );

                bind( Bean.class ).annotatedWith( Names.named( "provider" ) ).toProvider( Providers.of( new BeanImpl() ) );

                bind( Bean.class ).annotatedWith( Names.named( "broken" ) ).toProvider( new DeferredProvider<Bean>()
                {
                    public Bean get()
                    {
                        throw new TypeNotPresentException( "", null );
                    }

                    public DeferredClass<Bean> getImplementationClass()
                    {
                        throw new TypeNotPresentException( "", null );
                    }
                } );

            }
        } );
    }

    @Test
    void testImplementationVisitor()
    {
        assertEquals( BeanImpl.class, Implementations.find( new UntargettedBinding<BeanImpl>()
        {
            public Key<BeanImpl> getKey()
            {
                return Key.get( BeanImpl.class );
            }

            public Provider<BeanImpl> getProvider()
            {
                return null;
            }

            public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super BeanImpl, V> visitor )
            {
                return visitor.visit( this );
            }

            public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
            {
                return null;
            }

            public Object getSource()
            {
                return null;
            }

            public <T> T acceptVisitor( final ElementVisitor<T> visitor )
            {
                return null;
            }

            public void applyTo( final Binder binder )
            {
            }
        } ) );

        final Iterator<Binding<Bean>> itr = injector.findBindingsByType( TypeLiteral.get( Bean.class ) ).iterator();

        assertEquals( BeanImpl.class, Implementations.find( itr.next() ) ); // linked
        assertEquals( BeanImpl.class, Implementations.find( itr.next() ) ); // ctor
        assertEquals( BeanImpl.class, Implementations.find( itr.next() ) ); // instance
        assertEquals( BeanImpl.class, Implementations.find( itr.next() ) ); // deferred
        assertEquals( BeanImpl.class, Implementations.find( itr.next() ) ); // exposed

        assertNull( Implementations.find( itr.next() ) ); // provider instance
        assertNull( Implementations.find( itr.next() ) ); // broken provider

        assertFalse( itr.hasNext() );
    }
}
