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
package org.eclipse.sisu.plexus;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperties;
import org.eclipse.sisu.bean.BeanProperty;

import junit.framework.TestCase;

public class PlexusAnnotatedBeanMetadataTest
    extends TestCase
{
    @Component( role = Bean.class )
    protected static class Bean
    {
        @Configuration( name = "1", value = "BLANK" )
        String fixed;

        @Configuration( name = "2", value = "${some.value}" )
        String variable;

        String dummy1;

        @Requirement( role = Bean.class, hint = "mock", optional = true )
        Bean self;

        String dummy2;
    }

    @SuppressWarnings( "deprecation" )
    public void testRawAnnotations()
    {
        final PlexusBeanMetadata metadata = new PlexusAnnotatedMetadata( null );
        assertFalse( metadata.isEmpty() );

        final Iterator<BeanProperty<Object>> propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Requirement requirement2 = metadata.getRequirement( propertyIterator.next() );
        final Requirement requirement1 = metadata.getRequirement( propertyIterator.next() );
        final Configuration configuration3 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration2 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration1 = metadata.getConfiguration( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration1 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration1 );
        assertFalse( configuration2 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "${some.value}" ), configuration2 );
        assertNull( configuration3 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement1 );
        assertNull( requirement2 );
    }

    @SuppressWarnings( "deprecation" )
    public void testInterpolatedAnnotations()
    {
        final Map<?, ?> variables = Collections.singletonMap( "some.value", "INTERPOLATED" );

        final PlexusBeanMetadata metadata = new PlexusAnnotatedMetadata( variables );
        assertFalse( metadata.isEmpty() );

        final Iterator<BeanProperty<Object>> propertyIterator = new BeanProperties( Bean.class ).iterator();
        final Requirement requirement2 = metadata.getRequirement( propertyIterator.next() );
        final Requirement requirement1 = metadata.getRequirement( propertyIterator.next() );
        final Configuration configuration3 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration2 = metadata.getConfiguration( propertyIterator.next() );
        final Configuration configuration1 = metadata.getConfiguration( propertyIterator.next() );
        assertFalse( propertyIterator.hasNext() );

        assertFalse( configuration1 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "1", "BLANK" ), configuration1 );
        assertTrue( configuration2 instanceof ConfigurationImpl );
        assertEquals( new ConfigurationImpl( "2", "INTERPOLATED" ), configuration2 );
        assertNull( configuration3 );
        assertEquals( new RequirementImpl( Bean.class, true, "mock" ), requirement1 );
        assertNull( requirement2 );
    }
}
