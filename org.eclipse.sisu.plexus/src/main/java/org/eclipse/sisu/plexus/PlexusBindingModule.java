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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.sisu.bean.BeanListener;
import org.eclipse.sisu.bean.BeanManager;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

/**
 * Guice {@link Module} that supports registration, injection, and management of Plexus beans.
 */
public final class PlexusBindingModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanManager manager;

    private final PlexusBeanModule[] modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule( final BeanManager manager, final PlexusBeanModule... modules )
    {
        this.manager = manager;
        this.modules = modules.clone();
    }

    public PlexusBindingModule( final BeanManager manager, final Collection<? extends PlexusBeanModule> modules )
    {
        this.manager = manager;
        this.modules = modules.toArray( new PlexusBeanModule[modules.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final List<PlexusBeanSource> sources = new ArrayList<PlexusBeanSource>( modules.length );
        for ( final PlexusBeanModule module : modules )
        {
            final PlexusBeanSource source = module.configure( binder );
            if ( null != source )
            {
                sources.add( source );
            }
        }

        // attach custom logic to support Plexus requirements/configuration/lifecycle
        final PlexusBeanBinder plexusBinder = new PlexusBeanBinder( manager, sources );
        binder.bindListener( Matchers.any(), new BeanListener( plexusBinder ) );
        if ( manager instanceof Module )
        {
            ( (Module) manager ).configure( binder );
        }
    }
}
