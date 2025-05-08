/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.basic;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.Test;

import java.io.File;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnumConverterTest
{
    @Test
    public void canConvertPositive()
    {
        assertTrue( new EnumConverter().canConvert( StandardCopyOption.class ) );
    }

    @Test
    public void canConvertNegative()
    {
        assertFalse( new EnumConverter().canConvert( Object.class ) );
    }

    @Test
    public void testConvert()
        throws ComponentConfigurationException
    {
        XmlPlexusConfiguration config = new XmlPlexusConfiguration( "field" );
        config.setValue( StandardCopyOption.ATOMIC_MOVE.name() );
        Object object = new EnumConverter().fromConfiguration( null, config, StandardCopyOption.class, null, null,
                                                               new ExpressionEvaluator()
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
        assertTrue( object instanceof StandardCopyOption );
        assertEquals( StandardCopyOption.ATOMIC_MOVE, object );
    }
}