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
package org.eclipse.sisu.plexus.binders;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.binders.SpaceModule;
import org.eclipse.sisu.plexus.config.PlexusBeanMetadata;
import org.eclipse.sisu.plexus.config.PlexusBeanModule;
import org.eclipse.sisu.plexus.config.PlexusBeanSource;
import org.eclipse.sisu.plexus.scanners.PlexusAnnotatedMetadata;
import org.eclipse.sisu.plexus.scanners.PlexusTypeVisitor;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.scanners.ClassSpaceVisitor;
import org.eclipse.sisu.BeanScanning;

import com.google.inject.Binder;

/**
 * {@link PlexusBeanModule} that registers Plexus beans by scanning classes for runtime annotations.
 */
public final class PlexusAnnotatedBeanModule
    implements PlexusBeanModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final BeanScanning scanning;

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
        this.space = space;
        this.variables = variables;
        this.scanning = scanning;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PlexusBeanSource configure( final Binder binder )
    {
        if ( null != space && scanning != BeanScanning.OFF )
        {
            new PlexusSpaceModule( space, scanning ).configure( binder );
        }
        return new PlexusAnnotatedBeanSource( variables );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class PlexusSpaceModule
        extends SpaceModule
    {
        PlexusSpaceModule( final ClassSpace space, final BeanScanning scanning )
        {
            super( space, scanning );
        }

        @Override
        protected ClassSpaceVisitor visitor( final Binder binder )
        {
            return new PlexusTypeVisitor( new PlexusTypeBinder( binder ) );
        }
    }

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
