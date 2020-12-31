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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LoggerManager;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice {@link Module} that provides Plexus semantics without the full-blown Plexus container.
 */
public final class PlexusSpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final BeanScanning scanning;

    private BeanManager delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusSpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.OFF );
    }

    public PlexusSpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        this.space = space;
        this.scanning = scanning;
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

        final BeanManager manager =
            delegate instanceof PlexusLifecycleManager ? delegate
                                                       : new PlexusLifecycleManager( binder.getProvider( Context.class ),
                                                                                     binder.getProvider( LoggerManager.class ),
                                                                                     slf4jLoggerFactoryProvider,
                                                                                     delegate );

        binder.bind( BeanManager.class ).toInstance( manager );

        final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

        final Map<?, ?> variables = new ContextMapAdapter( context );
        beanModules.add( new PlexusXmlBeanModule( space, variables ) );
        beanModules.add( new PlexusAnnotatedBeanModule( space, variables, scanning ) );

        binder.install( new PlexusBindingModule( manager, beanModules ) );
    }

    /**
     * Delegate management of non-Plexus beans to the given {@link BeanManager}.
     */
    public PlexusSpaceModule with( final BeanManager manager )
    {
        delegate = manager;
        return this;
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
        @SuppressWarnings( { "rawtypes", "unchecked" } )
        protected void setParameters( @Parameters final Map parameters, final PlexusContainer container )
        {
            contextData.putAll( parameters );
            contextData.put( PlexusConstants.PLEXUS_KEY, container );
        }
    }
}
