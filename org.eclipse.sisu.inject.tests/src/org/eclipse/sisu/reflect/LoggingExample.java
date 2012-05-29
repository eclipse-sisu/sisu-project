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

import java.rmi.UnexpectedException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;

public class LoggingExample
{
    static class BadValue
    {
        @Override
        public String toString()
        {
            throw new RuntimeException( "sigh" );
        }
    }

    public LoggingExample()
    {
        Logs.debug( "", null, null );
        Logs.debug( "", "a", "b" );
        Logs.debug( "{", null, null );
        Logs.debug( "}", "a", "b" );
        Logs.debug( "}{", null, null );
        Logs.debug( "{}", "a", "b" );
        Logs.debug( "}{}", null, null );
        Logs.debug( "{{}", "a", "b" );
        Logs.debug( "{}{", null, null );
        Logs.debug( "{}}", "a", "b" );
        Logs.debug( "{{}}", null, null );
        Logs.debug( "}{}{", "a", "b" );
        Logs.debug( "{}{}", null, null );
        Logs.debug( "{}{}", "a", "b" );
        Logs.debug( "{{}}{{}}", null, null );
        Logs.debug( "{}-{}", "a", "b" );

        Logs.debug( "{} {}", new BadValue(), new BadValue() );

        Logs.debug( "Error: {} cause: {}", "oops", new UnexpectedException( "doh!" ) );

        Logs.warn( "", null, null );
        Logs.warn( "", "a", "b" );
        Logs.warn( "{", null, null );
        Logs.warn( "}", "a", "b" );
        Logs.warn( "}{", null, null );
        Logs.warn( "{}", "a", "b" );
        Logs.warn( "}{}", null, null );
        Logs.warn( "{{}", "a", "b" );
        Logs.warn( "{}{", null, null );
        Logs.warn( "{}}", "a", "b" );
        Logs.warn( "{{}}", null, null );
        Logs.warn( "}{}{", "a", "b" );
        Logs.warn( "{}{}", null, null );
        Logs.warn( "{}{}", "a", "b" );
        Logs.warn( "{{}}{{}}", null, null );
        Logs.warn( "{}-{}", "a", "b" );

        Logs.warn( "{} {}", new BadValue(), new BadValue() );

        Logs.warn( "Error: {} cause: {}", "oops", new UnexpectedException( "doh!" ) );

        final Module module = new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Object.class ).to( BadValue.class );
            }
        };

        Logs.debug( Logs.toString( module ), null, null );

        Logs.debug( Logs.toString( Guice.createInjector( module ) ), null, null );
    }
}
