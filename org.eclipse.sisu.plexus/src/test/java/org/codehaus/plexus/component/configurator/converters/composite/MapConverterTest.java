/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.composite;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MapConverterTest
{
    @Test
    public void canConvertPositive()
    {
        assertTrue( new MapConverter().canConvert( Map.class ) );
    }

    @Test
    public void canConvertNegative()
    {
        assertFalse( new MapConverter().canConvert( Object.class ) );
    }

    @Test
    public void testConvert()
        throws ComponentConfigurationException
    {
        XmlPlexusConfiguration config = new XmlPlexusConfiguration( "properties" );
        config.addChild( "key1", "value1" );
        config.addChild( "key2", "value2" );

        Object object =
            new MapConverter().fromConfiguration( null, config, HashMap.class, null, null, new ExpressionEvaluator()
            {
                @Override
                public Object evaluate( String expression )
                    throws ExpressionEvaluationException
                {
                    return expression;
                }

                @Override
                public File alignToBaseDirectory( File path )
                {
                    return null;
                }
            }, null );
        assertTrue( object instanceof HashMap );
        HashMap result = (HashMap) object;
        assertTrue( result.size() == 2 );
        assertEquals( "value1", result.get( "key1" ) );
        assertEquals( "value2", result.get( "key2" ) );
    }
}