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
package org.eclipse.sisu.plexus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.inject.DeferredProvider;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.LoadedClass;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.space.asm.ClassWriter;
import org.eclipse.sisu.space.asm.MethodVisitor;
import org.eclipse.sisu.space.asm.Opcodes;

import com.google.inject.TypeLiteral;

import junit.framework.TestCase;

public class PlexusXmlScannerTest
    extends TestCase
{
    static class NamedProperty
        implements BeanProperty<Object>
    {
        final String name;

        final TypeLiteral<Object> type;

        public NamedProperty( final String name )
        {
            this.name = name;
            type = TypeLiteral.get( Object.class );
        }

        @SuppressWarnings( { "unchecked", "rawtypes" } )
        public NamedProperty( final String name, final TypeLiteral type )
        {
            this.name = name;
            this.type = type;
        }

        public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
        {
            return null;
        }

        public String getName()
        {
            return name;
        }

        public TypeLiteral<Object> getType()
        {
            return type;
        }

        public <B> void set( final B bean, final Object value )
        {
        }
    }

    interface Bean
    {
    }

    protected static class DefaultBean
    {
    }

    static class DebugBean
    {
    }

    static class AnotherBean
    {
    }

    static class EmptyClassSpace
        implements ClassSpace
    {
        public Class<?> loadClass( final String name )
        {
            try
            {
                return Class.forName( name );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( name, e );
            }
        }

        @SuppressWarnings( { "unchecked", "rawtypes" } )
        public DeferredClass<?> deferLoadClass( final String name )
        {
            return new LoadedClass( loadClass( name ) );
        }

        public URL getResource( final String name )
        {
            return null;
        }

        public Enumeration<URL> getResources( final String name )
        {
            // hide components.xml so we can just test plexus.xml parsing
            return Collections.enumeration( Collections.<URL> emptyList() );
        }

        public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
        {
            // hide components.xml so we can just test plexus.xml parsing
            return Collections.enumeration( Collections.<URL> emptyList() );
        }
    }

    public void testLoadOnStart()
    {
        final URL plexusXml = getClass().getResource( "/META-INF/plexus/plexus.xml" );
        final PlexusXmlScanner scanner = new PlexusXmlScanner( null, plexusXml, null );

        final Map<Component, DeferredClass<?>> componentMap = scanner.scan( new EmptyClassSpace(), true );

        assertEquals( 2, componentMap.size() );

        final Component component1 =
            new ComponentImpl( DefaultBean.class, Hints.DEFAULT_HINT, Strategies.LOAD_ON_START, "" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).load() );

        final Component component2 =
            new ComponentImpl( Bean.class, "debug", Strategies.LOAD_ON_START, "For debugging" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).load() );
    }

    public void testBadPlexusXml()
    {
        final ClassSpace space = new URLClassSpace( PlexusXmlScannerTest.class.getClassLoader() );
        final URL plexusXml = getClass().getResource( "/META-INF/plexus/bad_plexus_1.xml" );
        new PlexusXmlScanner( null, plexusXml, null ).scan( space, true );
    }

    @SuppressWarnings( "deprecation" )
    public void testComponents()
    {
        final ClassSpace space = new URLClassSpace( PlexusXmlScannerTest.class.getClassLoader() );

        final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();
        final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, metadata );

        final Map<Component, DeferredClass<?>> componentMap = scanner.scan( space, true );

        assertEquals( 4, componentMap.size() );

        final Component component1 =
            new ComponentImpl( DefaultBean.class, Hints.DEFAULT_HINT, Strategies.PER_LOOKUP, "" );
        assertEquals( DefaultBean.class, componentMap.get( component1 ).load() );

        final Component component2 = new ComponentImpl( Bean.class, "debug", Strategies.SINGLETON, "For debugging" );
        assertEquals( DebugBean.class, componentMap.get( component2 ).load() );

        final Component component3 = new ComponentImpl( Bean.class, Hints.DEFAULT_HINT, Strategies.SINGLETON, "" );
        assertEquals( AnotherBean.class, componentMap.get( component3 ).load() );

        final Component component4 = new ComponentImpl( Bean.class, "clone", Strategies.SINGLETON, "" );
        assertEquals( DefaultBean.class, componentMap.get( component4 ).load().getSuperclass() );
        final Class<?> proxy = CustomTestClassLoader.proxy( componentMap.get( component4 ).load() );

        try
        {
            assertNotNull( proxy.getMethod( "TestMe" ) );
        }
        catch ( final NoSuchMethodException e )
        {
            fail( "Proxied class is missing 'TestMe' method" );
        }

        final PlexusBeanMetadata metadata1 = metadata.get( DefaultBean.class.getName() );

        assertFalse( metadata1.isEmpty() );

        assertEquals( new ConfigurationImpl( "someFieldName",
                                             "<some-field-name><item>PRIMARY</item></some-field-name>" ),
                      metadata1.getConfiguration( new NamedProperty( "someFieldName" ) ) );

        assertEquals( new ConfigurationImpl( "simple", "value" ),
                      metadata1.getConfiguration( new NamedProperty( "simple" ) ) );

        assertEquals( new ConfigurationImpl( "value", "<value with=\"attribute\"></value>" ),
                      metadata1.getConfiguration( new NamedProperty( "value" ) ) );

        assertEquals( new ConfigurationImpl( "emptyValue1", "<empty-value1 with=\"attribute\" />" ),
                      metadata1.getConfiguration( new NamedProperty( "emptyValue1" ) ) );

        assertEquals( new ConfigurationImpl( "emptyValue2", "" ),
                      metadata1.getConfiguration( new NamedProperty( "emptyValue2" ) ) );

        assertFalse( metadata1.isEmpty() );

        assertEquals( new RequirementImpl( Bean.class, true, "debug" ),
                      metadata1.getRequirement( new NamedProperty( "bean", TypeLiteral.get( Bean.class ) ) ) );

        assertFalse( metadata1.isEmpty() );

        metadata1.getConfiguration( new NamedProperty( "foo" ) );

        assertEquals( new RequirementImpl( Bean.class, false, Hints.DEFAULT_HINT, "debug" ),
                      metadata1.getRequirement( new NamedProperty( "beanMap" ) ) );

        assertFalse( metadata1.isEmpty() );

        assertEquals( new RequirementImpl( Bean.class, false ),
                      metadata1.getRequirement( new NamedProperty( "beanField" ) ) );

        assertTrue( metadata1.isEmpty() );

        assertNotNull( metadata.get( AnotherBean.class.getName() ) );
        assertNull( metadata.get( DebugBean.class.getName() ) );
    }

    static class FixedClassSpace
        implements ClassSpace
    {
        final String fixedResourceName;

        FixedClassSpace( final String fixedResourceName )
        {
            this.fixedResourceName = fixedResourceName;
        }

        public Class<?> loadClass( final String name )
        {
            try
            {
                return Class.forName( name );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( name, e );
            }
        }

        @SuppressWarnings( "rawtypes" )
        public DeferredClass<?> deferLoadClass( final String name )
        {
            return new DeferredClass()
            {
                public Class load()
                {
                    return loadClass( name );
                }

                public String getName()
                {
                    return name;
                }

                public DeferredProvider asProvider()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public URL getResource( final String name )
        {
            return getClass().getResource( fixedResourceName );
        }

        public Enumeration<URL> getResources( final String name )
        {
            return Collections.enumeration( Collections.singleton( getClass().getResource( fixedResourceName ) ) );
        }

        public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
        {
            return Collections.enumeration( Collections.singleton( getClass().getResource( fixedResourceName ) ) );
        }
    }

    public void testBadComponentsXml()
    {
        ClassSpace space;

        space = new FixedClassSpace( "/META-INF/plexus/bad_components_1.xml" );
        new PlexusXmlScanner( null, null, null ).scan( space, true );

        space = new FixedClassSpace( "/META-INF/plexus/bad_components_2.xml" );
        new PlexusXmlScanner( null, null, null ).scan( space, true );

        try
        {
            space = new FixedClassSpace( "/META-INF/plexus/bad_components_3.xml" );
            final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();
            final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, metadata );

            scanner.scan( space, true );

            final Requirement badReq =
                metadata.get( DefaultBean.class.getName() ).getRequirement( new NamedProperty( "no.such.class" ) );

            badReq.role();
            fail( "Expected TypeNotPresentException" );
        }
        catch ( final TypeNotPresentException e )
        {
        }

        space = new FixedClassSpace( "/META-INF/plexus/bad_components_4.xml" );
        final PlexusXmlScanner scanner = new PlexusXmlScanner( null, null, null );
        assertTrue( scanner.scan( space, true ).isEmpty() );
    }

    public void testInterpolatedComponentsXml()
    {
        final ClassSpace space = new FixedClassSpace( "/META-INF/plexus/variable_components.xml" );

        final Map<String, PlexusBeanMetadata> metadata = new HashMap<String, PlexusBeanMetadata>();

        new PlexusXmlScanner( null, null, metadata ).scan( space, true );

        assertEquals( "${some.value}",
                      metadata.get( DefaultBean.class.getName() ).getConfiguration( new NamedProperty( "variable" ) ).value() );

        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        new PlexusXmlScanner( variables, null, metadata ).scan( space, true );

        assertEquals( "INTERPOLATED",
                      metadata.get( DefaultBean.class.getName() ).getConfiguration( new NamedProperty( "variable" ) ).value() );
    }

    public void testLocalizedXmlScanning()
    {
        final ClassSpace space = new URLClassSpace( PlexusXmlScannerTest.class.getClassLoader(), null );

        assertFalse( new PlexusXmlScanner( null, null, null ).scan( space, true ).isEmpty() );
        assertTrue( new PlexusXmlScanner( null, null, null ).scan( space, false ).isEmpty() );
    }

    public void testOptionalLogging()
        throws Exception
    {
        final Level level = Logger.getLogger( "" ).getLevel();
        try
        {
            Logger.getLogger( "" ).setLevel( Level.SEVERE );

            // check everything still works without any SLF4J jars
            final ClassLoader noLoggingLoader =
                new URLClassLoader( new URLClassSpace( getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return PlexusXmlScannerTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noLoggingLoader.loadClass( SimpleScanningExample.class.getName() ).newInstance();
        }
        finally
        {
            Logger.getLogger( "" ).setLevel( level );
        }
    }

    static final class CustomTestClassLoader
        extends ClassLoader
    {
        private static final String PROXY_MARKER = "$proxy";

        CustomTestClassLoader( final ClassLoader parent )
        {
            super( parent );
        }

        static Class<?> proxy( final Class<?> clazz )
        {
            try
            {
                return new CustomTestClassLoader( clazz.getClassLoader() ).loadClass( clazz.getName() + PROXY_MARKER );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new TypeNotPresentException( clazz.getName(), e );
            }
        }

        @Override
        protected synchronized Class<?> loadClass( final String name, final boolean resolve )
            throws ClassNotFoundException
        {
            return super.loadClass( name, resolve );
        }

        @Override
        protected Class<?> findClass( final String name )
            throws ClassNotFoundException
        {
            final String proxyName = name.replace( '.', '/' );
            final String superName = proxyName.substring( 0, proxyName.length() - PROXY_MARKER.length() );

            final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
            cw.visit( Opcodes.V1_5, Modifier.PUBLIC | Modifier.FINAL, proxyName, null, superName, null );
            MethodVisitor mv = cw.visitMethod( Modifier.PUBLIC, "<init>", "()V", null, null );

            mv.visitCode();
            mv.visitVarInsn( Opcodes.ALOAD, 0 );
            mv.visitMethodInsn( Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false );
            mv.visitInsn( Opcodes.RETURN );
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();

            mv = cw.visitMethod( Modifier.PUBLIC, "TestMe", "()V", null, null );

            mv.visitCode();
            mv.visitInsn( Opcodes.RETURN );
            mv.visitMaxs( 0, 0 );
            mv.visitEnd();
            cw.visitEnd();

            final byte[] buf = cw.toByteArray();

            return defineClass( name, buf, 0, buf.length );
        }
    }
}
