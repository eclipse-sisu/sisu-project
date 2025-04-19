/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konrad Windszus - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.inject.CreationException;
import junit.framework.TestCase;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.sisu.plexus.component.Jsr330Component1;
import org.eclipse.sisu.plexus.component.Jsr330Component2;
import org.springframework.boot.test.context.FilteredClassLoader;

import static org.junit.Assert.assertThrows;

/**
 * Tests the behaviour when classloading fails due to invalid class files in the classpath (both with strict and non-strict classpath scanning).
 */
public class PlexusStrictClasspathScanningTest
    extends TestCase
{

    public void testStrictClasspathScanningWithValidClassFileForPlexusComponent()
        throws IOException, PlexusContainerException, ComponentLookupException
    {
        try ( TemporaryDirectoryClasspath dirClasspath = new TemporaryDirectoryClasspath() )
        {
            dirClasspath.addPackage( Paths.get( "target", "test-classes" ), "org.eclipse.sisu.plexus.component" );
            dirClasspath.addResource( Paths.get( "target", "test-classes", "invalid-plexus-components" ),
                                      "META-INF/plexus/components.xml" );

            Consumer<DefaultContainerConfiguration> configConsumer = config -> {
                config.setStrictClassPathScanning( true );
            };
            // test with container realm containing invalid class file
            DefaultPlexusContainer plexus =
                createIsolatedPlexusContainer( configConsumer, Optional.of( dirClasspath.getURL() ) );
            try
            {
                // unexpected wrapped exception: https://github.com/eclipse-sisu/sisu-project/issues/184
                plexus.lookup( Jsr330Component1.class.getName() );
                plexus.lookup( Jsr330Component2.class.getName() );
            }
            finally
            {
                if ( plexus != null )
                {
                    plexus.dispose();
                }
            }
        }
    }

    public void testStrictClasspathScanningWithInvalidClassFileForPlexusComponent()
        throws Exception
    {
        try ( TemporaryDirectoryClasspath dirClasspath = new TemporaryDirectoryClasspath() )
        {
            dirClasspath.addPackage( Paths.get( "target", "test-classes" ), "org.eclipse.sisu.plexus.component" );
            dirClasspath.addResource( Paths.get( "target", "test-classes", "invalid-plexus-components" ),
                                      "META-INF/plexus/components.xml" );
            dirClasspath.invalidateClassFile( Jsr330Component2.class.getName() );

            Consumer<DefaultContainerConfiguration> configConsumer = config -> {
                config.setStrictClassPathScanning( true );
            };
            // test with container realm containing invalid class file
            DefaultPlexusContainer plexus =
                createIsolatedPlexusContainer( configConsumer, Optional.of( dirClasspath.getURL() ) );
            try
            {
                // unexpected wrapped exception: https://github.com/eclipse-sisu/sisu-project/issues/184
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component1.class.getName() ) );
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component2.class.getName() ) );
            }
            finally
            {
                if ( plexus != null )
                {
                    plexus.dispose();
                }
            }

            // test with child realm containing invalid class file
            DefaultPlexusContainer plexus2 =
                createIsolatedPlexusContainer( configConsumer, Optional.of( dirClasspath.getURL() ) );
            try
            {
                final String realmId = "child-realm";
                ClassRealm realm = plexus2.createChildRealm( realmId );
                realm.addURL( dirClasspath.getURL() );
                plexus2.discoverComponents( realm ); // no exception here as only registered as Plexus component (not as
                                                     // Sisu component)
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component1.class.getName() ) );
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component2.class.getName() ) );
            }
            finally
            {
                if ( plexus != null )
                {
                    plexus2.dispose();
                }
            }
        }
    }

    public void testStrictClasspathScanningWithValidClassFileForSisuComponent()
        throws Exception
    {
        try ( TemporaryDirectoryClasspath dirClasspath = new TemporaryDirectoryClasspath() )
        {
            dirClasspath.addPackage( Paths.get( "target", "test-classes" ), "org.eclipse.sisu.plexus.component" );
            dirClasspath.addResource( Paths.get( "target", "test-classes", "invalid-sisu-components" ),
                                      "META-INF/sisu/javax.inject.Named" );

            Consumer<DefaultContainerConfiguration> configConsumer = config -> {
                config.setStrictClassPathScanning( true );
                config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
            };
            // test with container realm containing invalid class file
            DefaultPlexusContainer plexus =
                createIsolatedPlexusContainer( configConsumer, Optional.of( dirClasspath.getURL() ) );
            assertNotNull( plexus.lookup( Jsr330Component1.class.getName() ) );
        }
    }

    public void testStrictClasspathScanningWithInvalidClassFileForSisuComponent()
        throws Exception
    {
        try ( TemporaryDirectoryClasspath dirClasspath = new TemporaryDirectoryClasspath() )
        {
            dirClasspath.addPackage( Paths.get( "target", "test-classes" ), "org.eclipse.sisu.plexus.component" );
            dirClasspath.addResource( Paths.get( "target", "test-classes", "invalid-sisu-components" ),
                                      "META-INF/sisu/javax.inject.Named" );
            dirClasspath.invalidateClassFile( Jsr330Component2.class.getName() );

            Consumer<DefaultContainerConfiguration> configConsumer = config -> {
                config.setStrictClassPathScanning( true );
                config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
            };
            // test with container realm containing invalid class file
            CreationException e =
                assertThrows( CreationException.class,
                              () -> createIsolatedPlexusContainer( configConsumer,
                                                                   Optional.of( dirClasspath.getURL() ) ) );
            assertEquals( "Problem scanning " + dirClasspath.getClassFileUrl( Jsr330Component2.class.getName() ),
                          e.getCause().getMessage() );
            assertEquals( "Unsupported class file major version 255", e.getCause().getCause().getMessage() );

            // test with container realm containing invalid class file
            final String realmId = "child-realm";
            DefaultPlexusContainer plexus = createIsolatedPlexusContainer( configConsumer, Optional.empty() );
            try
            {
                ClassRealm realm = plexus.createChildRealm( realmId );
                realm.addURL( dirClasspath.getURL() );
                e = assertThrows( CreationException.class, () -> plexus.discoverComponents( realm ) );

                assertEquals( "Problem scanning " + dirClasspath.getClassFileUrl( Jsr330Component2.class.getName() ),
                              e.getCause().getMessage() );
                assertEquals( "Unsupported class file major version 255", e.getCause().getCause().getMessage() );
                // unexpected wrapped exception: https://github.com/eclipse-sisu/sisu-project/issues/184
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component1.class.getName() ) );
                assertThrows( ComponentLookupException.class, () -> plexus.lookup( Jsr330Component2.class.getName() ) );
            }
            finally
            {
                if ( plexus != null )
                {
                    plexus.dispose();
                }
            }
        }
    }

    private static DefaultPlexusContainer createIsolatedPlexusContainer( Consumer<DefaultContainerConfiguration> configConsumer,
                                                                         Optional<URL> classpathUrl )
        throws PlexusContainerException
    {
        DefaultContainerConfiguration config = new DefaultContainerConfiguration();
        configConsumer.accept( config );
        URL[] urls = classpathUrl.map( p -> new URL[] { p } ).orElse( new URL[0] );
        // create filtered classloader which excludes the test components (full isolation is not possible due to shared
        // usage of JSR 330 annotations and Java base classes)
        FilteredClassLoader filteredClassLoader =
            new FilteredClassLoader( name -> name.startsWith( "org/eclipse/sisu/plexus/component/" )
                || name.startsWith( "org.eclipse.sisu.plexus.component." ) );
        URLClassLoader classLoader = new URLClassLoader( urls, filteredClassLoader );
        ClassWorld classWorld = new ClassWorld( "plexus.core", classLoader );
        config.setClassWorld( classWorld );
        return new DefaultPlexusContainer( config );
    }

    /** Wrapper around a temporary directory which can be used as classpath for a {@link ClassLoader}. */
    private final class TemporaryDirectoryClasspath
        implements AutoCloseable
    {
        private final Path tempDir;

        public TemporaryDirectoryClasspath()
            throws IOException
        {
            tempDir = Files.createTempDirectory( "plexus-classpath" );
        }

        void addPackage( Path sourceDir, String packageName )
            throws IOException
        {
            if ( !Files.isDirectory( sourceDir ) )
            {
                throw new IllegalArgumentException( "Not a directory: " + sourceDir );
            }
            String relativePath = packageName.replace( '.', File.separatorChar );
            copyFolder( sourceDir.resolve( relativePath ), tempDir.resolve( relativePath ) );
        }

        void addResource( Path sourceDir, String resourceName )
            throws IOException
        {
            if ( !Files.isDirectory( sourceDir ) )
            {
                throw new IllegalArgumentException( "Not a directory: " + sourceDir );
            }
            Path source = sourceDir.resolve( resourceName );
            Path dest = tempDir.resolve( resourceName );
            Files.createDirectories( dest.getParent() );
            Files.copy( source, dest );
        }

        private void copyFolder( Path src, Path dest )
            throws IOException
        {
            Files.createDirectories( dest.getParent() );
            try ( Stream<Path> stream = Files.walk( src ) )
            {
                stream.forEachOrdered( sourcePath -> {
                    try
                    {
                        Path destPath = dest.resolve( src.relativize( sourcePath ) );
                        Files.copy( sourcePath, destPath );
                    }
                    catch ( IOException e )
                    {
                        throw new UncheckedIOException( e );
                    }
                } );
            }
            catch ( UncheckedIOException e )
            {
                throw e.getCause();
            }
        }

        URL getURL()
            throws MalformedURLException
        {
            return tempDir.toUri().toURL();
        }

        void invalidateClassFile( String className )
            throws IOException
        {
            // just write parts of class header with invalid major version (this leads to UnsupportedClassVersionError)
            byte[] data = new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF };
            String relativePath = className.replace( '.', File.separatorChar ) + ".class";
            Files.write( tempDir.resolve( relativePath ), data, StandardOpenOption.TRUNCATE_EXISTING );
        }

        public URL getClassFileUrl( String className )
        {
            String relativePath = className.replace( '.', File.separatorChar ) + ".class";
            try
            {
                return tempDir.resolve( relativePath ).toUri().toURL();
            }
            catch ( MalformedURLException e )
            {
                throw new IllegalArgumentException( "Invalid class name: " + className, e );
            }
        }

        @Override
        public void close()
            throws IOException
        {
            try ( Stream<Path> paths = Files.walk( tempDir ) )
            {
                paths.sorted( Comparator.reverseOrder() ).forEachOrdered( t -> {
                    try
                    {
                        Files.delete( t );
                    }
                    catch ( IOException e )
                    {
                        throw new UncheckedIOException( e );
                    }
                } );
            }
        }
    }
}
