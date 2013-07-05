/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.SpaceVisitor;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * {@link PlexusBeanModule} that registers Plexus beans by scanning classes for runtime annotations.
 */
public final class PlexusAnnotatedBeanModule
    implements PlexusBeanModule
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final SpaceModule.Strategy PLEXUS_STRATEGY = new SpaceModule.Strategy()
    {
        public SpaceVisitor visitor( final Binder binder )
        {
            return new PlexusTypeVisitor( new PlexusTypeBinder( binder ) );
        }
    };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Module spaceModule;

    private final PlexusBeanSource beanSource;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public PlexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables )
    {
        this( space, variables, BeanScanning.ON );
    }

    /**
     * Creates a bean source that scans the given class space for Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     * @param scanning The scanning options
     */
    public PlexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables, final BeanScanning scanning )
    {
        if ( null != space && scanning != BeanScanning.OFF )
        {
            spaceModule = new SpaceModule( space, scanning ).with( PLEXUS_STRATEGY );
        }
        else
        {
            spaceModule = null;
        }
        beanSource = new PlexusAnnotatedBeanSource( variables );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PlexusBeanSource configure( final Binder binder )
    {
        if ( null != spaceModule )
        {
            spaceModule.configure( binder );
        }
        return beanSource;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class PlexusAnnotatedBeanSource
        implements PlexusBeanSource
    {
        private final PlexusBeanMetadata metadata;

        PlexusAnnotatedBeanSource( final Map<?, ?> variables )
        {
            metadata = new PlexusAnnotatedMetadata( variables );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return implementation.isAnnotationPresent( Component.class ) ? metadata : null;
        }
    }
}
