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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.space.ClassSpace;

import com.google.inject.Binder;

/**
 * {@link PlexusBeanModule} that binds Plexus components by scanning XML resources.
 */
public final class PlexusXmlBeanModule
    implements PlexusBeanModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<?, ?> variables;

    private final URL plexusXml;

    private final boolean root;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans all the surrounding class spaces for XML resources.
     * 
     * @param space The main class space
     * @param variables The filter variables
     * @param plexusXml The plexus.xml URL
     */
    public PlexusXmlBeanModule( final ClassSpace space, final Map<?, ?> variables, final URL plexusXml )
    {
        this.space = space;
        this.variables = variables;
        this.plexusXml = plexusXml;
        root = true;
    }

    /**
     * Creates a bean source that only scans the local class space for XML resources.
     * 
     * @param space The local class space
     * @param variables The filter variables
     */
    public PlexusXmlBeanModule( final ClassSpace space, final Map<?, ?> variables )
    {
        this.space = space;
        this.variables = variables;
        plexusXml = null;
        root = false;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PlexusBeanSource configure( final Binder binder )
    {
        binder.bind( ClassSpace.class ).toInstance( space );

        final PlexusTypeBinder plexusTypeBinder = new PlexusTypeBinder( binder );
        final Map<String, PlexusBeanMetadata> metadataMap = new HashMap<String, PlexusBeanMetadata>();
        final PlexusXmlScanner scanner = new PlexusXmlScanner( variables, plexusXml, metadataMap );

        final String source = space.toString();
        for ( final Entry<Component, DeferredClass<?>> entry : scanner.scan( space, root ).entrySet() )
        {
            plexusTypeBinder.hear( entry.getKey(), entry.getValue(), source );
        }

        return new PlexusXmlBeanSource( metadataMap );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link PlexusBeanSource} backed by consumable XML metadata.
     */
    private static final class PlexusXmlBeanSource
        implements PlexusBeanSource
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Map<String, PlexusBeanMetadata> metadataMap;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        PlexusXmlBeanSource( final Map<String, PlexusBeanMetadata> metadataMap )
        {
            this.metadataMap = metadataMap;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( null == metadataMap )
            {
                return null;
            }
            final PlexusBeanMetadata metadata = metadataMap.remove( implementation.getName() );
            if ( metadataMap.isEmpty() )
            {
                metadataMap = null;
            }
            return metadata;
        }
    }
}
