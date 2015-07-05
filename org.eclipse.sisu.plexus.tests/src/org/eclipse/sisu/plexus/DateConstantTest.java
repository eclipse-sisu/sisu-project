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
package org.eclipse.sisu.plexus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class DateConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bind( final String name, final String value )
            {
                bindConstant().annotatedWith( Names.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Format1", "2005-10-06 2:22:55.1 PM" );
                bind( "Format2", "2005-10-06 2:22:55PM" );
                bind( "BadFormat", "2005-10-06" );

                install( new PlexusDateTypeConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Format1" )
    String dateText1;

    @Inject
    @Named( "Format1" )
    Date date1;

    @Inject
    @Named( "Format2" )
    String dateText2;

    @Inject
    @Named( "Format2" )
    Date date2;

    @Inject
    Injector injector;

    public void testDateFormat1()
    {
        assertEquals( dateText1, new SimpleDateFormat( "yyyy-MM-dd h:mm:ss.S a", Locale.US ).format( date1 ) );
    }

    public void testDateFormat2()
    {
        assertEquals( dateText2, new SimpleDateFormat( "yyyy-MM-dd h:mm:ssa", Locale.US ).format( date2 ) );
    }

    public void testBadDateFormat()
    {
        try
        {
            injector.getInstance( Key.get( Date.class, Names.named( "BadFormat" ) ) );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
        }
    }
}
