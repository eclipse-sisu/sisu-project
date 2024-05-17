/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BasicComponentConfiguratorTest
{
    @Rule
    public TemporaryFolder tmpDirectory = new TemporaryFolder();

    private ComponentConfigurator configurator;

    @Before
    public void setUp()
    {
        configurator = new BasicComponentConfigurator();
    }

    @Test
    public void testSimplePathOnDefaultFileSystem()
        throws ComponentConfigurationException
    {
        PathTestComponent component = new PathTestComponent();
        Path absolutePath = Paths.get( "" ).resolve( "absolute" ).toAbsolutePath();
        configure( component, "path", "readme.txt", "absolutePath", absolutePath.toString(), "file", "readme.txt",
                   "absoluteFile", absolutePath.toString() );
        // path must be converted to absolute one
        assertEquals( tmpDirectory.getRoot().toPath().resolve( "readme.txt" ), component.path );
        assertEquals( FileSystems.getDefault(), component.path.getFileSystem() );
        assertEquals( absolutePath, component.absolutePath );
        assertEquals( new File( tmpDirectory.getRoot(), "readme.txt" ), component.file );
        assertEquals( absolutePath.toFile(), component.absoluteFile );
    }

    @Test
    public void testTypeWithoutConverterButConstructorAcceptingString()
        throws ComponentConfigurationException, IOException
    {
        CustomTypeComponent component = new CustomTypeComponent();
        configure( component, "custom", "hello world" );
        assertEquals( "hello world", component.custom.toString() );
    }

    @Test
    public void testTemporalConvertersWithoutMillisecondsAndOffset()
        throws ComponentConfigurationException
    {
        TemporalComponent component = new TemporalComponent();
        String dateString = "2023-01-02 03:04:05";
        configure( component, "localDateTime", dateString, "localDate", dateString, "localTime", dateString, "instant",
                   dateString, "offsetDateTime", dateString, "offsetTime", dateString, "zonedDateTime", dateString );
        assertEquals( LocalDateTime.of( 2023, 1, 2, 3, 4, 5, 0 ), component.localDateTime );
        assertEquals( LocalDate.of( 2023, 1, 2 ), component.localDate );
        assertEquals( LocalTime.of( 3, 4, 5, 0 ), component.localTime );
        ZoneOffset systemOffset = ZoneId.systemDefault().getRules().getOffset( component.localDateTime );
        assertEquals( OffsetDateTime.of( component.localDateTime, systemOffset ).toInstant(), component.instant );
        assertEquals( OffsetDateTime.of( component.localDateTime, systemOffset ), component.offsetDateTime );
        assertEquals( OffsetTime.of( component.localTime, systemOffset ), component.offsetTime );
        assertEquals( ZonedDateTime.of( component.localDateTime, ZoneId.systemDefault() ), component.zonedDateTime );
    }

    @Test
    public void testTemporalConvertersWithISO8601StringWithOffset()
        throws ComponentConfigurationException
    {
        TemporalComponent component = new TemporalComponent();
        String dateString = "2023-01-02T03:04:05.000000900+02:30";
        configure( component, "localDateTime", dateString, "localDate", dateString, "localTime", dateString, "instant",
                   dateString, "offsetDateTime", dateString, "offsetTime", dateString, "zonedDateTime", dateString );
        assertEquals( LocalDateTime.of( 2023, 1, 2, 3, 4, 5, 900 ), component.localDateTime );
        assertEquals( LocalDate.of( 2023, 1, 2 ), component.localDate );
        assertEquals( LocalTime.of( 3, 4, 5, 900 ), component.localTime );
        ZoneOffset offset = ZoneOffset.ofHoursMinutes( 2, 30 );
        assertEquals( OffsetDateTime.of( component.localDateTime, offset ).toInstant(), component.instant );
        assertEquals( OffsetDateTime.of( component.localDateTime, offset ), component.offsetDateTime );
        assertEquals( OffsetTime.of( component.localTime, offset ), component.offsetTime );
        assertEquals( ZonedDateTime.of( component.localDateTime, offset ), component.zonedDateTime );
    }

    @Test
    public void testTemporalConvertersWithInvalidString()
        throws ComponentConfigurationException
    {
        TemporalComponent component = new TemporalComponent();
        String dateString = "invalid";
        assertThrows( ComponentConfigurationException.class,
                      () -> configure( component, "localDateTime", dateString, "localDate", dateString, "localTime",
                                       dateString, "instant", dateString, "offsetDateTime", dateString, "offsetTime",
                                       dateString, "zonedDateTime", dateString ) );
    }

    @Test
    public void testConfigureComplexBean()
        throws Exception
    {
        ComplexBean complexBean = new ComplexBean();

        // configure( complexBean, "resources", "foo;bar" );
        DefaultPlexusConfiguration config = new DefaultPlexusConfiguration( "testConfig" );

        DefaultPlexusConfiguration child = new DefaultPlexusConfiguration( "resources", "foo;bar" );
        child.setAttribute( "implementation", "java.lang.String" );

        config.addChild( child );

        configure( complexBean, config,
                   new ClassWorld( "foo", Thread.currentThread().getContextClassLoader() ).getClassRealm( "foo" ) );

        assertEquals( complexBean.resources.size(), 2 );
        assertTrue( complexBean.resources.toString(), complexBean.resources.contains( Resource.newResource( "foo" ) ) );
        assertTrue( complexBean.resources.toString(), complexBean.resources.contains( Resource.newResource( "bar" ) ) );
    }

    private void configure( Object component, String... keysAndValues )
        throws ComponentConfigurationException
    {
        final DefaultPlexusConfiguration config = new DefaultPlexusConfiguration( "testConfig" );
        if ( keysAndValues.length % 2 != 0 )
        {
            throw new IllegalArgumentException( "Even number of keys and values expected" );
        }
        for ( int i = 0; i < keysAndValues.length; i += 2 )
        {
            config.addChild( keysAndValues[i], keysAndValues[i + 1] );
        }
        configure( component, config );
    }

    private void configure( Object component, PlexusConfiguration config )
        throws ComponentConfigurationException
    {
        configure( component, config, null );
    }

    private void configure( Object component, PlexusConfiguration config, ClassRealm loader )
        throws ComponentConfigurationException
    {
        final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator()
        {
            @Override
            public File alignToBaseDirectory( File path )
            {
                if ( !path.isAbsolute() )
                {
                    return new File( tmpDirectory.getRoot(), path.getPath() );
                }
                else
                {
                    return path;
                }
            }
        };
        configurator.configureComponent( component, config, evaluator, loader );
    }

    static final class PathTestComponent
    {
        Path path;

        Path absolutePath;

        File file;

        File absoluteFile;
    }

    public static final class CustomType
    {
        private final String input;

        public CustomType()
        {
            this.input = "invalid";
        }

        public CustomType( String input )
        {
            this.input = input;
        }

        @Override
        public String toString()
        {
            return input;
        }
    }

    static final class CustomTypeComponent
    {
        CustomType custom;
    }

    static final class TemporalComponent
    {
        LocalDateTime localDateTime;

        LocalDate localDate;

        LocalTime localTime;

        Instant instant;

        OffsetDateTime offsetDateTime;

        OffsetTime offsetTime;

        ZonedDateTime zonedDateTime;
    }

    static final class ComplexBean
    {
        private List<Resource> resources;

        public void setResources( List<Resource> resources )
        {
            this.resources = resources;
        }

        public void setResources( String resources )
        {
            this.resources =
                Arrays.stream( resources.split( ";" ) ).map( Resource::newResource ).collect( Collectors.toList() );
        }

    }

    static abstract class Resource
    {
        String path;

        static Resource newResource( String path )
        {
            return new BaseResource( path );
        }

        static class BaseResource
            extends Resource
        {
            public BaseResource( String path )
            {
                this.path = path;
            }
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;
            Resource resource = (Resource) o;
            return Objects.equals( path, resource.path );
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode( path );
        }
    }
}