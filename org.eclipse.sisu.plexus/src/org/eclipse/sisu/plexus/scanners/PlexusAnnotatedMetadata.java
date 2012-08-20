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
package org.eclipse.sisu.plexus.scanners;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.sisu.plexus.annotations.ConfigurationImpl;
import org.eclipse.sisu.plexus.config.PlexusBeanMetadata;
import org.eclipse.sisu.reflect.BeanProperty;

/**
 * Runtime {@link PlexusBeanMetadata} based on {@link BeanProperty} annotations.
 */
public final class PlexusAnnotatedMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<?, ?> variables;

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
            final String value = StringUtils.interpolate( uninterpolatedValue, variables );
            if ( !value.equals( uninterpolatedValue ) )
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
}
