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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

public class DefaultValuesTest
    extends TestCase
{
    public void testDefaultBasicValue()
        throws ComponentConfigurationException
    {
        final ComponentWithInt componentWithString = new ComponentWithInt();

        final PlexusConfiguration config = new XmlPlexusConfiguration( "config" );
        final PlexusConfiguration target = new XmlPlexusConfiguration( "target" );
        target.setAttribute( "default-value", "TEST" );
        config.addChild( target );

        new BasicComponentConfigurator().configureComponent( componentWithString, config, null );
        assertEquals( "TEST", componentWithString.target );

        target.setValue( "OVERRIDE" );

        new BasicComponentConfigurator().configureComponent( componentWithString, config, null );
        assertEquals( "OVERRIDE", componentWithString.target );
    }

    public void testDefaultCollection()
        throws ComponentConfigurationException
    {
        final ComponentWithArray componentWithArray = new ComponentWithArray();
        final ComponentWithList componentWithList = new ComponentWithList();

        final PlexusConfiguration config = new XmlPlexusConfiguration( "config" );
        final PlexusConfiguration target = new XmlPlexusConfiguration( "target" );
        target.setAttribute( "default-value", "one,two,three" );
        config.addChild( target );

        new BasicComponentConfigurator().configureComponent( componentWithArray, config, null );
        assertTrue( Arrays.equals( new String[] { "one", "two", "three" }, componentWithArray.target ) );

        new BasicComponentConfigurator().configureComponent( componentWithList, config, null );
        assertEquals( Arrays.asList( "one", "two", "three" ), componentWithList.target );

        final PlexusConfiguration element = new XmlPlexusConfiguration( "element" );
        element.setValue( "OVERRIDE" );
        target.addChild( element );

        new BasicComponentConfigurator().configureComponent( componentWithArray, config, null );
        assertTrue( Arrays.equals( new String[] { "OVERRIDE" }, componentWithArray.target ) );

        new BasicComponentConfigurator().configureComponent( componentWithList, config, null );
        assertEquals( Arrays.asList( "OVERRIDE" ), componentWithList.target );
    }

    static class ComponentWithInt
    {
        String target;
    }

    static class ComponentWithArray
    {
        String[] target;
    }

    static class ComponentWithList
    {
        List<String> target;
    }
}
