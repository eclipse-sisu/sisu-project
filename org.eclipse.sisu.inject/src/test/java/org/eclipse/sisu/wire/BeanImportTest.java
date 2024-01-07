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
package org.eclipse.sisu.wire;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Dynamic;
import org.eclipse.sisu.Nullable;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.Sources;
import org.eclipse.sisu.inject.TypeArguments;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.URLClassSpace;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
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

class BeanImportTest
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

    public interface Y
    {
        double fn( double x, double y );
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
    public interface ImplicitY
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

    @Named( "CustomName" )
    public static class ImplWithName
    {
    }

    public static abstract class AbstractY
        implements Y
    {
        public double fn( final double x, final double y )
        {
            return x + y;
        }
    }

    public static class YImpl
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

        // @Inject
        // Logger logger;

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
        @Named( "CustomName" )
        ImplWithName implWithName;

        @Inject
        Map<Annotation, Y> annotatedMap;

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
        Collection<Y> coll;

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
        Iterable<BeanEntry<?, Y>> entries;

        @Inject
        Iterable<BeanEntry<Named, Y>> namedEntries;
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

    static class DynamicInstance
        implements X
    {
        @Inject
        @Dynamic
        Y interfaceProxy;

        @Inject
        @Dynamic
        YImpl concreteProxy;
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

            bind( X.class ).annotatedWith( Names.named( "DI" ) ).to( DynamicInstance.class );

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

            bind( ImplWithName.class );

            bind( ParameterKeys.PROPERTIES ).toInstance( PROPS );
        }
    }

    @Test
    void testUnrestrictedImport()
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

        final Object[] listArray = unrestrictedList.list.toArray();
        final Object[] collArray = unrestrictedList.coll.toArray();

        assertSame( listArray[0], collArray[0] );
        assertNotSame( listArray[1], collArray[1] );
        assertSame( listArray[2], collArray[2] );

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

    @Test
    void testNamedImports()
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

    @Test
    void testProviderImports()
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

    @Test
    void testBeanEntries()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final BeanEntries beans = (BeanEntries) injector.getInstance( Key.get( X.class, Names.named( "BE" ) ) );
        final HintMap hintMap = (HintMap) injector.getInstance( Key.get( X.class, Names.named( "HM" ) ) );

        Iterator<? extends BeanEntry<?, Y>> i = beans.namedEntries.iterator();

        assertTrue( i.hasNext() );
        assertSame( hintMap.map.get( "fixed" ), i.next().getValue() );
        assertNotSame( hintMap.map.get( "unscoped" ), i.next().getValue() );
        assertFalse( i.hasNext() );

        i = beans.entries.iterator();

        assertTrue( i.hasNext() );
        assertEquals( Names.named( "fixed" ), i.next().getKey() );
        assertNotSame( Names.named( "unscoped" ), i.next().getKey() );
        assertNotSame( new FuzzyImpl(), i.next().getKey() );
        assertFalse( i.hasNext() );
    }

    @Test
    void testPlaceholderImports()
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

    @Test
    void testDuplicatesAreIgnored()
    {
        Guice.createInjector( new WireModule( new TestModule(), new TestModule(), new TestModule() ) );
    }

    @Test
    void testImportSource()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );
        assertEquals( LocatorWiring.class.getName(), injector.getBinding( Y.class ).getSource().toString() );
    }

    @Test
    void testInvalidTypeArguments()
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

    @Test
    void testGenericInjection()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final GenericInstance genericInstance =
            (GenericInstance) injector.getInstance( Key.get( X.class, Names.named( "GI" ) ) );

        // ZImpl<Integer> is best match for Z<? extends Number>
        assertEquals( TypeLiteral.get( Integer.class ),
                      TypeArguments.get( TypeLiteral.get( genericInstance.number.getClass() ).getSupertype( Z.class ),
                                         0 ) );

        // ZImpl<String> is best match for Z<String>
        assertEquals( TypeLiteral.get( String.class ),
                      TypeArguments.get( TypeLiteral.get( genericInstance.chars.getClass() ).getSupertype( Z.class ),
                                         0 ) );

        // raw ZImpl is best match for Z<Random>
        assertEquals( TypeLiteral.get( Object.class ),
                      TypeArguments.get( TypeLiteral.get( genericInstance.random.getClass() ).getSupertype( Z.class ),
                                         0 ) );
    }

    @Test
    void testChildWiring()
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

        assertSame( y,
                    ( (PlaceholderString) grandchild.getInstance( Key.get( X.class, Names.named( "PS" ) ) ) ).fixed );
        assertSame( y,
                    ( (PlaceholderString) grandchild.getInstance( Key.get( X.class, Names.named( "PS" ) ) ) ).fuzzy );
    }

    @Test
    void testParametersLookup()
    {
        final BeanLocator locator = Guice.createInjector( new WireModule( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( ParameterKeys.PROPERTIES ).toInstance( Collections.singletonMap( "Hello", "world!" ) );
            }
        } ) ).getInstance( BeanLocator.class );

        @SuppressWarnings( { "rawtypes", "unchecked" } )
        final Iterator<Map<?, ?>> itr = new EntryListAdapter( locator.locate( ParameterKeys.PROPERTIES ) ).iterator();

        assertTrue( itr.hasNext() );

        final Map<?, ?> parameters = itr.next();
        assertEquals( 1, parameters.size() );
        assertEquals( "world!", parameters.get( "Hello" ) );

        assertFalse( itr.hasNext() );
    }

    @SuppressWarnings( "boxing" )
    @Test
    void testDynamicProxy()
    {
        final Injector injector = Guice.createInjector( new WireModule( new TestModule() ) );

        final DynamicInstance dynamicInstance =
            (DynamicInstance) injector.getInstance( Key.get( X.class, Names.named( "DI" ) ) );

        assertEquals( 42.0, dynamicInstance.interfaceProxy.fn( 12.3, 29.7 ) );

        assertEquals( 9.0, dynamicInstance.concreteProxy.fn( 7, 2 ) );

        // add new Y binding that multiplies the arguments instead of adding them
        final Injector child1 = injector.createChildInjector( new ChildWireModule( injector, new AbstractModule()
        {
            @Override
            protected void configure()
            {
                final Binder overrides = binder().withSource( Sources.prioritize( Integer.MAX_VALUE ) );
                overrides.bind( Y.class ).annotatedWith( Names.named( "multiply" ) ).toInstance( new YImpl()
                {
                    @Override
                    public double fn( final double x, final double y )
                    {
                        return x * y;
                    }
                } );
            }
        } ) );

        // interface proxy should now delegate to multiplying implementation
        assertEquals( 365.31, dynamicInstance.interfaceProxy.fn( 12.3, 29.7 ) );

        // concrete proxy shouldn't be affected by the interface binding
        assertEquals( 9.0, dynamicInstance.concreteProxy.fn( 7, 2 ) );

        // add new YImpl binding that divides the arguments instead of adding them
        final Injector child2 = injector.createChildInjector( new ChildWireModule( injector, new AbstractModule()
        {
            @Override
            protected void configure()
            {
                final Binder overrides = binder().withSource( Sources.prioritize( Integer.MAX_VALUE ) );
                overrides.bind( YImpl.class ).annotatedWith( Names.named( "divide" ) ).toInstance( new YImpl()
                {
                    @Override
                    public double fn( final double x, final double y )
                    {
                        return x / y;
                    }
                } );
            }
        } ) );

        // interface proxy should still delegate to multiplier implementation
        assertEquals( 365.31, dynamicInstance.interfaceProxy.fn( 12.3, 29.7 ) );

        // concrete proxy should now delegate to the dividing implementation
        assertEquals( 3.5, dynamicInstance.concreteProxy.fn( 7, 2 ) );

        // Object.toString delegates to the active instance
        final Y multiply = child1.getInstance( Key.get( Y.class, Names.named( "multiply" ) ) );
        final Y divide = child2.getInstance( Key.get( YImpl.class, Names.named( "divide" ) ) );
        assertTrue( dynamicInstance.interfaceProxy.toString().equals( multiply.toString() ) );
        assertTrue( dynamicInstance.concreteProxy.toString().equals( divide.toString() ) );

        // but Object.hashCode and Object.equals default to superclass instead
        assertEquals( System.identityHashCode( dynamicInstance.interfaceProxy ),
                      dynamicInstance.interfaceProxy.hashCode() );
        assertEquals( System.identityHashCode( dynamicInstance.concreteProxy ),
                      dynamicInstance.concreteProxy.hashCode() );

        assertTrue( dynamicInstance.interfaceProxy.equals( dynamicInstance.interfaceProxy ) );
        assertTrue( dynamicInstance.concreteProxy.equals( dynamicInstance.concreteProxy ) );

        // remove all implementations from the shared locator
        injector.getInstance( MutableBeanLocator.class ).clear();

        try
        {
            dynamicInstance.interfaceProxy.fn( 12.3, 29.7 );
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException e )
        {
            // should now get an exception on invoke
        }

        try
        {
            dynamicInstance.concreteProxy.fn( 7, 2 );
            fail( "Expected IllegalStateException" );
        }
        catch ( final IllegalStateException e )
        {
            // should now get an exception on invoke
        }

        // all Object methods default to superclass implementation when delegate is missing
        assertTrue( dynamicInstance.interfaceProxy.toString().startsWith( Y.class.getName() + "$__sisu__$" ) );
        assertTrue( dynamicInstance.concreteProxy.toString().startsWith( YImpl.class.getName() + "$__sisu__$" ) );

        assertEquals( System.identityHashCode( dynamicInstance.interfaceProxy ),
                      dynamicInstance.interfaceProxy.hashCode() );
        assertEquals( System.identityHashCode( dynamicInstance.concreteProxy ),
                      dynamicInstance.concreteProxy.hashCode() );

        assertTrue( dynamicInstance.interfaceProxy.equals( dynamicInstance.interfaceProxy ) );
        assertTrue( dynamicInstance.concreteProxy.equals( dynamicInstance.concreteProxy ) );
    }
}
