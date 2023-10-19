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

import java.net.URL;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class URLTypeConverterTest
{
    @Test
    void testURLConversion()
    {
        final URL url = Guice.createInjector( new URLTypeConverter(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "url" ) ).to( "http://127.0.0.1/" );
            }
        } ).getInstance( Key.get( URL.class, Names.named( "url" ) ) );

        assertEquals( "http://127.0.0.1/", url.toString() );
    }

    @Test
    void testBrokenURLConversion()
    {
        try
        {
            Guice.createInjector( new URLTypeConverter(), new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bindConstant().annotatedWith( Names.named( "url" ) ).to( "foo^bar" );
                }
            } ).getInstance( Key.get( URL.class, Names.named( "url" ) ) );
            fail( "Expected ConfigurationException" );
        }
        catch ( final ConfigurationException e )
        {
        }
    }
}
