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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Nullable;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.TypeParameters;
import org.eclipse.sisu.reflect.URLClassSpace;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public class BeanImportTest
    extends TestCase
{
    @Target( FIELD )
    @Retention( RUNTIME )
    @BindingAnnotation
    public @interface Fuzzy
    {
    }

    static class FuzzyImpl
        implements Fuzzy
    {
        public Class<? extends Annotation> annotationType()
        {
            return Fuzzy.class;
        }

        @Override
        public boolean equals( final Object rhs )
        {
            return rhs instanceof Fuzzy;
        }

        @Override
        public int hashCode()
        {
            return 0;
        }
    }

    interface X
    {
    }

    interface Y
    {
    }

    interface Z<T>
    {
    }

    @ProvidedBy( XProvider.class )
    interface ImplicitX
        extends X
    {
    }

    @ImplementedBy( YImpl.class )
    interface ImplicitY
        extends Y
    {
    }

    static class XProvider
        implements Provider<ImplicitX>
    {
        public ImplicitX get()
        {
            return new ImplicitX()
            {
            };
        }
    }

    static abstract class AbstractY
        implements Y
    {
    }

    static class YImpl
        extends AbstractY
        implements ImplicitY
    {
    }

    static class ZImpl<T>
        implements Z<T>
    {
        T element;
    }

    static abstract class AbstractX
        implements X
    {
        @Inject
        Injector injector;

        @Inject
        Logger logger;

        @Inject
        ImplicitX implicitX;

        @Inject
        ImplicitY implicitY;

        @Inject
        YImpl concreteY;

        @Inject
        @Nullable
        AbstractY abstractY;

        @Inject
        @Fuzzy
        Y fuzzy;

        @Inject
        @Named( "fixed" )
        Y fixed;

        @Inject
        Map<Named, Y> namedMap;

        @Inject
        Map<Named, Provider<Y>> namedProviderMap;
    }

    static class UnrestrictedInstance
        extends AbstractX
    {
        final Y single;

        @Inject
        UnrestrictedInstance( @Nullable final Y single, @Named( "fixed" ) final Y fixed )
        {
            this.single = single;
            this.fixed = fixed;
        }
    }

    static class UnrestrictedList
        extends AbstractX
    {
        @Inject
        List<Y> list;

        @Inject
        Iterable<Y> iterable;

        @Inject
        List<Provider<Y>> providerList;
    }

    static class UnrestrictedSet
        extends AbstractX
    {
        @Inject
        Set<Y> set;

        @Inject
        Set<Provider<Y>> providerSet;
    }

    static class NamedType
        extends AbstractX
    {
        final Y single;

        @Inject
        NamedType( @Named( "fixed" ) final Y fixed, @Nullable @Named final Y single )
        {
            this.single = single;
            this.fixed = fixed;
        }
    }

    static class NamedInstance
        extends AbstractX
    {
        final Y single;

        @Inject
        NamedInstance( @Nullable @Named( "TEST" ) final Y single )
        {
            this.single = single;
        }

        @Inject
        void setFixed( final @Named( "fixed" ) Y fixed )
        {
            this.fixed = fixed;
        }
    }

    static class HintMap
        extends AbstractX
    {
        @Inject
        Map<String, Y> map;

        @Inject
        Map<String, Provider<Y>> providerMap;
    }

    static class BeanEntries
        implements X
    {
        @Inject
        Iterable<BeanEntry<Named, Y>> entries;
    }

    static class PlaceholderInstance
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "${name}" )
        Y single;
    }

    static class PlaceholderString
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "${text}" )
        String config;

        @Inject
        @Nullable
        @Named( "text" )
        String plain;
    }

    static class PlaceholderConfig
        extends AbstractX
    {
        @Inject
        @Nullable
        @Named( "4${value}2" )
        int single;
    }

    static class BadMap
        implements X
    {
        @Inject
        Map<Integer, Integer> map;
    }

    static class RawList
        implements X
    {
        @Inject
        @SuppressWarnings( "rawtypes" )
        List list;
    }

    static class RawMap
        implements X
    {
        @Inject
        @SuppressWarnings( "rawtypes" )
        Map map;
    }

    static class MissingList
        implements X
    {
        @Inject
        @Named( "missing" )
        List<Y> list;
    }

    static class MissingSet
        implements X
    {
        @Inject
        @Named( "missing" )
        Set<Y> set;
    }

    static class MissingMap
        implements X
    {
        @Inject
        @Named( "missing" )
        Map<Named, Y> map;
    }

    static class GenericInstance
        implements X
    {
        @Inject
        Z<? extends Number> number;

        @Inject
        Z<String> chars;

        @Inject
        Z<Random> random;
    }

    static Map<String, Object> PROPS = new HashMap<String, Object>();

    class TestModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind( ClassSpace.class ).toInstance( new URLClassSpace( BeanImportTest.class.getClassLoader() ) );

            bindInterceptor( Matchers.subclassesOf( X.class ), Matchers.any() );
            requestInjection( BeanImportTest.this );

            bind( X.class ).annotatedWith( Names.named( "UI" ) ).to( UnrestrictedInstance.class );
            bind( X.class ).annotatedWith( Names.named( "UL" ) ).to( UnrestrictedList.class );
            bind( X.class ).annotatedWith( Names.named( "US" ) ).to( UnrestrictedSet.class );

            bind( X.class ).annotatedWith( Names.named( "NT" ) ).to( NamedType.class );
            bind( X.class ).annotatedWith( Names.named( "NI" ) ).to( NamedInstance.class );
            bind( X.class ).annotatedWith( Names.named( "HM" ) ).to( HintMap.class );

            bind( X.class ).annotatedWith( Names.named( "BE" ) ).to( BeanEntries.class );

            bind( X.class ).annotatedWith( Names.named( "PI" ) ).to( PlaceholderInstance.class );
            bind( X.class ).annotatedWith( Names.named( "PS" ) ).to( PlaceholderString.class );
            bind( X.class ).annotatedWith( Names.named( "PC" ) ).to( PlaceholderConfig.class );

            bind( X.class ).annotatedWith( Names.named( "GI" ) ).to( GenericInstance.class );

            bind( Y.class ).annotatedWith( Names.named( "fixed" ) ).toInstance( new YImpl() );
            bind( Y.class ).annotatedWith( Names.named( "unscoped" ) ).to( YImpl.class );
            bind( Y.class ).annotatedWith( new FuzzyImpl() ).toInstance( new YImpl() );

            bind( Z.class ).annotatedWith( Names.named( "integer" ) ).toInstance( new ZImpl<Integer>()
            {
            } );
            bind( Z.class ).annotatedWith( Names.named( "string" ) ).toInstance( new ZImpl<String>()
            {
            } );
            bind( Z.class ).annotatedWith( Names.named( "raw" ) ).to( ZImpl.class );

            bind( ParameterKeys.PROPERTIES ).toInstance( PROPS );
        }
    }

    public void testUnrestrictedImport()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final UnrestrictedInstance unrestrictedInstance =
            (UnrestrictedInstance) injector.getInstance( Key.get( X.class, Names.named( "UI" ) ) );

        assertSame( unrestrictedInstance.fixed, unrestrictedInstance.single );

        final UnrestrictedList unrestrictedList =
            (UnrestrictedList) injector.getInstance( Key.get( X.class, Names.named( "UL" ) ) );

        assertEquals( 3, unrestrictedList.list.size() );

        assertSame( unrestrictedInstance.fixed, unrestrictedList.list.get( 0 ) );
        assertSame( unrestrictedList.fixed, unrestrictedList.list.get( 0 ) );

        assertSame( unrestrictedInstance.fuzzy, unrestrictedList.list.get( 2 ) );
        assertSame( unrestrictedList.fuzzy, unrestrictedList.list.get( 2 ) );

        assertNotSame( unrestrictedList.list.get( 0 ), unrestrictedList.list.get( 2 ) );

        final Iterator<?> iterator = unrestrictedList.iterable.iterator();

        assertTrue( iterator.hasNext() );
        assertSame( unrestrictedList.list.get( 0 ), iterator.next() );
        iterator.next();
        assertSame( unrestrictedList.list.get( 2 ), iterator.next() );
        assertFalse( iterator.hasNext() );

        final UnrestrictedSet unrestrictedSet =
            (UnrestrictedSet) injector.getInstance( Key.get( X.class, Names.named( "US" ) ) );

        assertEquals( 3, unrestrictedSet.set.size() );

        assertTrue( unrestrictedSet.set.contains( unrestrictedInstance.fixed ) );
        assertTrue( unrestrictedSet.set.contains( unrestrictedList.fixed ) );

        assertTrue( unrestrictedSet.set.contains( unrestrictedInstance.fuzzy ) );
        assertTrue( unrestrictedSet.set.contains( unrestrictedList.fuzzy ) );
    }

    public void testNamedImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final NamedType namedType = (NamedType) injector.getInstance( Key.get( X.class, Names.named( "NT" ) ) );
        final NamedInstance namedInstance =
            (NamedInstance) injector.getInstance( Key.get( X.class, Names.named( "NI" ) ) );

        assertNotNull( namedType.single );
        assertSame( namedType.fixed, namedType.single );
        assertNull( namedInstance.single );

        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );
        assertSame( namedType.fixed, hintMap.map.get( "fixed" ) );
        assertSame( hintMap.fixed, hintMap.map.get( "fixed" ) );
        assertNotSame( namedType.fixed, hintMap.map.get( "unscoped" ) );
        assertNotSame( hintMap.fixed, hintMap.map.get( "unscoped" ) );
        assertEquals( 2, hintMap.map.size() );
    }

    public void testProviderImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final UnrestrictedList unrestrictedList =
            (UnrestrictedList) injector.getInstance( Key.get( X.class, Names.named( "UL" ) ) );

        final UnrestrictedSet unrestrictedSet =
            (UnrestrictedSet) injector.getInstance( Key.get( X.class, Names.named( "US" ) ) );

        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );

        Provider<Y> provider;

        provider = unrestrictedList.providerList.get( 0 );
        assertTrue( provider.get() instanceof YImpl );
        assertSame( provider.get(), provider.get() );
        provider = unrestrictedList.providerList.get( 1 );
        assertTrue( provider.get() instanceof YImpl );
        assertNotSame( provider.get(), provider.get() );
        assertEquals( 3, unrestrictedList.providerList.size() );

        final Iterator<Provider<Y>> itr = unrestrictedSet.providerSet.iterator();

        provider = itr.next();
        assertTrue( provider.get() instanceof YImpl );
        assertSame( provider.get(), provider.get() );
        provider = itr.next();
        assertTrue( provider.get() instanceof YImpl );
        assertNotSame( provider.get(), provider.get() );
        assertEquals( 3, unrestrictedList.providerList.size() );

        provider = hintMap.providerMap.get( "unscoped" );
        assertTrue( provider.get() instanceof YImpl );
        assertNotSame( provider.get(), provider.get() );
        assertEquals( 2, hintMap.providerMap.size() );

        provider = hintMap.namedProviderMap.get( Names.named( "unscoped" ) );
        assertTrue( provider.get() instanceof YImpl );
        assertNotSame( provider.get(), provider.get() );
        assertEquals( 2, hintMap.namedProviderMap.size() );

        provider = hintMap.providerMap.get( "fixed" );
        assertTrue( provider.get() instanceof YImpl );
        assertSame( provider.get(), provider.get() );
        assertEquals( 2, hintMap.providerMap.size() );

        provider = hintMap.namedProviderMap.get( Names.named( "fixed" ) );
        assertTrue( provider.get() instanceof YImpl );
        assertSame( provider.get(), provider.get() );
        assertEquals( 2, hintMap.namedProviderMap.size() );
    }

    public void testBeanEntries()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final BeanEntries beans = (BeanEntries) injector.getInstance( Key.get( X.class, Names.named( "BE" ) ) );
        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );

        final Iterator<BeanEntry<Named, Y>> i = beans.entries.iterator();

        assertTrue( i.hasNext() );
        assertSame( hintMap.map.get( "fixed" ), i.next().getValue() );
        assertNotSame( hintMap.map.get( "unscoped" ), i.next().getValue() );
        assertFalse( i.hasNext() );
    }

    public void testPlaceholderImports()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        PlaceholderInstance placeholderInstance;
        placeholderInstance = (PlaceholderInstance) injector.getInstance( Key.get( X.class, Names.named( "PI" ) ) );
        assertNull( placeholderInstance.single );

        final Y why = new YImpl();
        PROPS.put( "name", why );

        placeholderInstance = (PlaceholderInstance) injector.getInstance( Key.get( X.class, Names.named( "PI" ) ) );
        assertSame( why, placeholderInstance.single );

        PROPS.put( "name", "fixed" );

        placeholderInstance = (PlaceholderInstance) injector.getInstance( Key.get( X.class, Names.named( "PI" ) ) );
        assertSame( placeholderInstance.fixed, placeholderInstance.single );

        PlaceholderString placeholderString;
        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertNull( placeholderString.config );
        assertNull( placeholderString.plain );

        PROPS.put( "text", "Hello, world!" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "Hello, world!", placeholderString.config );
        assertEquals( "Hello, world!", placeholderString.plain );

        PROPS.put( "text", "text" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "text", placeholderString.config );
        assertEquals( "text", placeholderString.plain );

        PROPS.put( "text", "${text}" );

        try
        {
            placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            assertTrue( e.getMessage().contains( "${text}" ) );
        }

        PROPS.put( "text", ">${one}{" );
        PROPS.put( "one", "-${two}=" );
        PROPS.put( "two", "<${three}}" );
        PROPS.put( "three", "|${text}|" );

        try
        {
            placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
            assertTrue( e.getMessage().contains( ">-<|>-<|${text}|}={|}={" ) );
        }

        PROPS.put( "text", ">${text" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( ">${text", placeholderString.config );
        assertEquals( ">${text", placeholderString.plain );

        PROPS.put( "text", "${key:-default}" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "default", placeholderString.config );
        assertEquals( "default", placeholderString.plain );

        PROPS.put( "key", "configured" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "configured", placeholderString.config );
        assertEquals( "configured", placeholderString.plain );

        PROPS.put( "text", "${:-some:-default:-value:-}" );

        placeholderString = (PlaceholderString) injector.getInstance( Key.get( X.class, Names.named( "PS" ) ) );
        assertEquals( "some:-default:-value:-", placeholderString.config );
        assertEquals( "some:-default:-value:-", placeholderString.plain );

        try
        {
            injector.getInstance( Key.get( X.class, Names.named( "PC" ) ) );
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
            System.out.println( e );
        }

        PROPS.put( "value", "53" );

        assertEquals( 4532,
                      ( (PlaceholderConfig) injector.getInstance( Key.get( X.class, Names.named( "PC" ) ) ) ).single );
    }

    public void testDuplicatesAreIgnored()
    {
        Guice.createInjector( new WireModule( new TestModule(), new TestModule(), new TestModule() ) );
    }

    public void testImportSource()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );
        assertEquals( LocatorWiring.class.getName(), injector.getBinding( Y.class ).getSource().toString() );
    }

    public void testInvalidTypeParameters()
    {
        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "BM" ) ).to( BadMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "RL" ) ).to( RawList.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "RM" ) ).to( RawMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "ML" ) ).to( MissingList.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "MS" ) ).to( MissingSet.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }

        try
        {
            Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( X.class ).annotatedWith( Names.named( "MM" ) ).to( MissingMap.class );
                }
            } ) );
            fail( "Expected CreationException" );
        }
        catch ( final CreationException e )
        {
        }
    }

    public void testGenericInjection()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final GenericInstance genericInstance =
            (GenericInstance) injector.getInstance( Key.get( X.class, Names.named( "GI" ) ) );

        assertEquals( TypeLiteral.get( Integer.class ),
                      TypeParameters.get( TypeLiteral.get( genericInstance.number.getClass() ).getSupertype( Z.class ),
                                          0 ) );

        assertEquals( TypeLiteral.get( String.class ),
                      TypeParameters.get( TypeLiteral.get( genericInstance.chars.getClass() ).getSupertype( Z.class ),
                                          0 ) );

        assertEquals( ZImpl.class, genericInstance.random.getClass() );
    }

    public void testChildWiring()
    {
        final Y y = new YImpl();

        final Injector parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Y.class ).annotatedWith( Names.named( "fixed" ) ).toInstance( y );
            }
        } );

        final Injector child = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Y.class ).annotatedWith( new FuzzyImpl() ).toInstance( y );
            }
        } );

        final Injector grandchild = child.createChildInjector( new ChildWireModule( child, new TestModule() ) );

        assertSame( y, ( (PlaceholderString) grandchild.getInstance( Key.get( X.class, Names.named( "PS" ) ) ) ).fixed );
        assertSame( y, ( (PlaceholderString) grandchild.getInstance( Key.get( X.class, Names.named( "PS" ) ) ) ).fuzzy );
    }
}
