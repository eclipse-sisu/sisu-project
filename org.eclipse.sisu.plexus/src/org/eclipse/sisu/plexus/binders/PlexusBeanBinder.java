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

import java.util.List;

import org.eclipse.sisu.inject.BeanBinder;
import org.eclipse.sisu.inject.PropertyBinder;
import org.eclipse.sisu.plexus.config.PlexusBeanMetadata;
import org.eclipse.sisu.plexus.config.PlexusBeanSource;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanBinder} that binds bean properties according to Plexus metadata.
 */
final class PlexusBeanBinder
    implements BeanBinder, InjectionListener<Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanManager manager;

    private final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusBeanBinder( final PlexusBeanManager manager, final List<PlexusBeanSource> sources )
    {
        this.manager = manager;
        this.sources = sources.toArray( new PlexusBeanSource[sources.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final Class<?> clazz = type.getRawType();
        if ( null != manager && manager.manage( clazz ) )
        {
            encounter.register( this );
        }
        for ( final PlexusBeanSource source : sources )
        {
            // use first source that has metadata for the given implementation
            final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
            if ( metadata != null )
            {
                return new PlexusPropertyBinder( manager, encounter, metadata );
            }
        }
        return null; // no need to auto-bind
    }

    public void afterInjection( final Object bean )
    {
        manager.manage( bean );
    }
}
