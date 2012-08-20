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
package org.eclipse.sisu.plexus.shim;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LoggerManager;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.plexus.binders.PlexusBeanManager;
import org.eclipse.sisu.plexus.binders.PlexusBindingModule;
import org.eclipse.sisu.plexus.binders.PlexusXmlBeanModule;
import org.eclipse.sisu.plexus.config.PlexusBeanConverter;
import org.eclipse.sisu.plexus.config.PlexusBeanLocator;
import org.eclipse.sisu.plexus.config.PlexusBeanModule;
import org.eclipse.sisu.plexus.converters.PlexusXmlBeanConverter;
import org.eclipse.sisu.plexus.lifecycles.PlexusLifecycleManager;
import org.eclipse.sisu.plexus.locators.DefaultPlexusBeanLocator;
import org.eclipse.sisu.Parameters;

import com.google.inject.Binder;
import com.google.inject.Module;

public final class PlexusSpaceModule
    implements Module
{
    private final ClassSpace space;

    public PlexusSpaceModule( final ClassSpace space )
    {
        this.space = space;
    }

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

    static final class ParameterizedContext
        extends DefaultContext
    {
        @Inject
        protected void setParameters( @Parameters final Map<?, ?> parameters )
        {
            contextData.putAll( parameters );
        }
    }
}
