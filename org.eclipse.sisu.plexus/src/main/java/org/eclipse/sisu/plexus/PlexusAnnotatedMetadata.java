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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.eclipse.sisu.bean.BeanProperty;

/**
 * Runtime {@link PlexusBeanMetadata} based on {@link BeanProperty} annotations.
 */
public final class PlexusAnnotatedMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @SuppressWarnings( "rawtypes" )
    private final Map variables;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Provides runtime Plexus metadata based on simple property annotations.
     *
     * @param variables The filter variables
     */
    public PlexusAnnotatedMetadata( final Map<?, ?> variables )
    {
        this.variables = variables;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return false; // metadata comes from the properties themselves
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        final Configuration configuration = property.getAnnotation( Configuration.class );
        if ( configuration != null && variables != null )
        {
            // support runtime interpolation of @Configuration values
            final String uninterpolatedValue = configuration.value();
            final String value = interpolate( uninterpolatedValue );
            if ( null != value && !value.equals( uninterpolatedValue ) )
            {
                return new ConfigurationImpl( configuration.name(), value );
            }
        }
        return configuration;
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        return property.getAnnotation( Requirement.class );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private String interpolate( final String text )
    {
        if ( null == text || !text.contains( "${" ) )
        {
            return text;
        }
        // use same interpolation method as XML for sake of consistency
        try ( Reader r = new InterpolationFilterReader( new StringReader( text ), variables ) ) {
            return IOUtil.toString(r);
        } catch ( IOException e ) {
            return text; // should never actually happen, as no actual I/O involved
        }
    }
}
