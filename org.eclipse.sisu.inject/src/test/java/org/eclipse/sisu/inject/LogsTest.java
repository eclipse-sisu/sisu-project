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

import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.sisu.BaseTests;
import org.eclipse.sisu.space.URLClassSpace;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.SimpleLogger;

@BaseTests
class LogsTest
{
    @Test
    void testLogging()
    {
        new LoggingExample();
    }

    @Test
    void testProductionLogging()
        throws Exception
    {
        try
        {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");

            final ClassLoader productionLoader =
                new URLClassLoader( new URLClassSpace( getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.startsWith( "org.slf4j" ) || name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            productionLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
        }
    }

    @Test
    void testFallBackToJDKLogging()
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

            final ClassLoader noSLF4JLoader =
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
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noSLF4JLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            rootLogger.setLevel( level );
        }
    }

    @Test
    void testConsoleLogging()
        throws Exception
    {
        System.setProperty( "org.eclipse.sisu.log", "console" );
        try
        {
            final ClassLoader consoleLoader =
                new URLClassLoader( new URLClassSpace( getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            consoleLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            System.clearProperty( "org.eclipse.sisu.log" );
        }
    }
}
