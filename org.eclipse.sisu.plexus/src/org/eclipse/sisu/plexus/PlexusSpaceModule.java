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

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LoggerManager;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.space.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * {@link Module} that provides Plexus semantics without the full-blown Plexus container.
 */
public final class PlexusSpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusSpaceModule( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final Context context = new ParameterizedContext();
        binder.bind( Context.class ).toInstance( context );

        final Provider<?> slf4jLoggerFactoryProvider = space.deferLoadClass( "org.slf4j.ILoggerFactory" ).asProvider();
        binder.requestInjection( slf4jLoggerFactoryProvider );

        binder.bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
        binder.bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
        binder.bind( PlexusContainer.class ).to( PseudoPlexusContainer.class );

        final PlexusBeanManager manager = new PlexusLifecycleManager( binder.getProvider( Context.class ), //
                                                                      binder.getProvider( LoggerManager.class ), //
                                                                      slf4jLoggerFactoryProvider ); // SLF4J (optional)

        binder.bind( PlexusBeanManager.class ).toInstance( manager );

        final PlexusBeanModule xmlModule = new PlexusXmlBeanModule( space, new ContextMapAdapter( context ) );
        binder.install( new PlexusBindingModule( manager, xmlModule ) );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link Context} backed by Sisu {@link Parameters}.
     */
    static final class ParameterizedContext
        extends DefaultContext
    {
        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------

        @Inject
        protected void setParameters( @Parameters final Map<?, ?> parameters )
        {
            contextData.putAll( parameters );
        }
    }
}
