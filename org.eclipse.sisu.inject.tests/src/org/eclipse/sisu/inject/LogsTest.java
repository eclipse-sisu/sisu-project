/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

public class LogsTest
    extends TestCase
{
    public static final ClassLoader WITH_SLF4J = isolatedClassLoader( false );

    public static final ClassLoader WITHOUT_SLF4J = isolatedClassLoader( true );

    public void testLogging()
    {
        new LoggingExample();
    }

    public void testProductionLogging()
        throws Exception
    {
        try
        {
            ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( Logs.class ) ).setLevel( ch.qos.logback.classic.Level.WARN );

            WITH_SLF4J.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            // no-op
        }
    }

    public void testFallBackToJDKLogging()
        throws Exception
    {
        final Logger rootLogger = Logger.getLogger( "" );

        final Handler[] handlers = rootLogger.getHandlers();
        if ( handlers.length > 0 )
        {
            handlers[0].setLevel( Level.FINE );
        }

        final Level level = rootLogger.getLevel();
        try
        {
            rootLogger.setLevel( Level.FINE );

            WITHOUT_SLF4J.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            rootLogger.setLevel( level );
        }
    }

    public void testConsoleLogging()
        throws Exception
    {
        System.setProperty( "org.eclipse.sisu.log", "console" );
        try
        {
            WITH_SLF4J.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            System.clearProperty( "org.eclipse.sisu.log" );
        }
    }

    private static ClassLoader isolatedClassLoader( final boolean hideSLF4j )
    {
        return new URLClassLoader( systemClassPath(), null )
        {
            @Override
            protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                throws ClassNotFoundException
            {
                if ( hideSLF4j && name.contains( "slf4j" ) )
                {
                    throw new ClassNotFoundException( name );
                }
                if ( !hideSLF4j && name.startsWith( "ch" ) || name.contains( "cobertura" ) )
                {
                    return LogsTest.class.getClassLoader().loadClass( name );
                }
                return super.loadClass( name, resolve );
            }
        };
    }

    private static URL[] systemClassPath()
    {
        final ClassLoader testClassLoader = LogsTest.class.getClassLoader();
        if ( testClassLoader instanceof URLClassLoader )
        {
            return ( (URLClassLoader) testClassLoader ).getURLs();
        }
        final List<URL> urls = new ArrayList<URL>();
        for ( final String path : System.getProperty( "java.class.path", "." ).split( File.pathSeparator ) )
        {
            try
            {
                urls.add( new File( path ).getAbsoluteFile().toURI().toURL() );
            }
            catch ( final MalformedURLException e )
            {
                // skip bad classpath entry
            }
        }
        return urls.toArray( new URL[urls.size()] );
    }
}
