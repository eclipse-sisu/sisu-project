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
package org.eclipse.sisu.plexus.lifecycles;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.eclipse.sisu.inject.PropertyBinding;
import org.eclipse.sisu.plexus.binders.PlexusBeanManager;
import org.eclipse.sisu.reflect.BeanProperty;
import org.eclipse.sisu.reflect.Logs;

import com.google.inject.spi.ProvisionListener;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
public final class PlexusLifecycleManager
    implements PlexusBeanManager, ProvisionListener
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Class<?>[] LIFECYCLE_TYPES = { LogEnabled.class, Contextualizable.class, Initializable.class,
        Startable.class, Disposable.class };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ThreadLocal<List<?>[]> pendingHolder = new ThreadLocal<List<?>[]>()
    {
        @Override
        protected List<?>[] initialValue()
        {
            return new List[1];
        }
    };

    private final List<Startable> startableBeans = new ArrayList<Startable>();

    private final List<Disposable> disposableBeans = new ArrayList<Disposable>();

    private final Logger consoleLogger = new ConsoleLogger();

    private final Provider<Context> plexusContextProvider;

    private final Provider<LoggerManager> plexusLoggerManagerProvider;

    private final Provider<?> slf4jLoggerFactoryProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusLifecycleManager( final Provider<Context> plexusContextProvider,
                                   final Provider<LoggerManager> plexusLoggerManagerProvider,
                                   final Provider<?> slf4jLoggerFactoryProvider )
    {
        this.plexusContextProvider = plexusContextProvider;
        this.plexusLoggerManagerProvider = plexusLoggerManagerProvider;
        this.slf4jLoggerFactoryProvider = slf4jLoggerFactoryProvider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Class<?> clazz )
    {
        for ( final Class<?> lifecycleType : LIFECYCLE_TYPES )
        {
            if ( lifecycleType.isAssignableFrom( clazz ) )
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings( "rawtypes" )
    public PropertyBinding manage( final BeanProperty property )
    {
        final Class clazz = property.getType().getRawType();
        if ( "org.slf4j.Logger".equals( clazz.getName() ) )
        {
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, getSLF4JLogger( bean ) );
                }
            };
        }
        if ( Logger.class.equals( clazz ) )
        {
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, getPlexusLogger( bean ) );
                }
            };
        }
        return null;
    }

    public <T> void onProvision( final ProvisionInvocation<T> pi )
    {
        final List<?>[] holder = pendingHolder.get();
        if ( null == holder[0] )
        {
            List<?> beans;
            holder[0] = Collections.EMPTY_LIST;
            try
            {
                pi.provision();
            }
            finally
            {
                beans = holder[0];
                holder[0] = null;
            }

            for ( int i = 0, size = beans.size(); i < size; i++ )
            {
                manageLifecycle( beans.get( i ) );
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public boolean manage( final Object bean )
    {
        if ( bean instanceof Disposable )
        {
            synchronizedAdd( disposableBeans, (Disposable) bean );
        }
        if ( bean instanceof LogEnabled )
        {
            ( (LogEnabled) bean ).enableLogging( getPlexusLogger( bean ) );
        }
        if ( bean instanceof Contextualizable || bean instanceof Initializable || bean instanceof Startable )
        {
            final List<?>[] holder = pendingHolder.get();
            List beans = holder[0];
            if ( null == beans || beans.isEmpty() )
            {
                holder[0] = beans = new ArrayList<Object>();
            }
            beans.add( bean );
        }
        return true;
    }

    public boolean unmanage( final Object bean )
    {
        if ( synchronizedRemove( startableBeans, bean ) )
        {
            stop( (Startable) bean );
        }
        if ( synchronizedRemove( disposableBeans, bean ) )
        {
            dispose( (Disposable) bean );
        }
        return true;
    }

    public boolean unmanage()
    {
        for ( Startable bean; ( bean = synchronizedRemoveLast( startableBeans ) ) != null; )
        {
            stop( bean );
        }
        for ( Disposable bean; ( bean = synchronizedRemoveLast( disposableBeans ) ) != null; )
        {
            dispose( bean );
        }
        pendingHolder.remove();
        return true;
    }

    public PlexusBeanManager manageChild()
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Shared implementation methods
    // ----------------------------------------------------------------------

    Logger getPlexusLogger( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            return plexusLoggerManagerProvider.get().getLoggerForComponent( name, null );
        }
        catch ( final RuntimeException e )
        {
            return consoleLogger;
        }
    }

    Object getSLF4JLogger( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            return ( (org.slf4j.ILoggerFactory) slf4jLoggerFactoryProvider.get() ).getLogger( name );
        }
        catch ( final RuntimeException e )
        {
            return org.slf4j.LoggerFactory.getLogger( name );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static <T> boolean synchronizedAdd( final List<T> list, final T element )
    {
        synchronized ( list )
        {
            return list.add( element );
        }
    }

    private static boolean synchronizedRemove( final List<?> list, final Object element )
    {
        synchronized ( list )
        {
            return list.remove( element );
        }
    }

    private static <T> T synchronizedRemoveLast( final List<T> list )
    {
        synchronized ( list )
        {
            final int size = list.size();
            if ( size > 0 )
            {
                return list.remove( size - 1 );
            }
            return null;
        }
    }

    private void manageLifecycle( final Object bean )
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
            {
                // need to check hierarchy in case bean is proxied
                final ClassLoader loader = clazz.getClassLoader();
                if ( loader instanceof URLClassLoader )
                {
                    Thread.currentThread().setContextClassLoader( loader );
                    break;
                }
            }
            /*
             * Run through the startup phase of the standard plexus "personality"
             */
            if ( bean instanceof Contextualizable )
            {
                contextualize( (Contextualizable) bean );
            }
            if ( bean instanceof Initializable )
            {
                initialize( (Initializable) bean );
            }
            if ( bean instanceof Startable )
            {
                // register before calling start in case it fails
                final Startable startableBean = (Startable) bean;
                synchronizedAdd( startableBeans, startableBean );
                start( startableBean );
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    private void contextualize( final Contextualizable bean )
    {
        Logs.debug( "Contextualize: <>", bean, null );
        try
        {
            bean.contextualize( plexusContextProvider.get() );
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                getPlexusLogger( this ).warn( "Error contextualizing: " + Logs.identityToString( bean ), e );
            }
            finally
            {
                Logs.throwUnchecked( e );
            }
        }
    }

    private void initialize( final Initializable bean )
    {
        Logs.debug( "Initialize: <>", bean, null );
        try
        {
            bean.initialize();
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                getPlexusLogger( this ).warn( "Error initializing: " + Logs.identityToString( bean ), e );
            }
            finally
            {
                Logs.throwUnchecked( e );
            }
        }
    }

    private void start( final Startable bean )
    {
        Logs.debug( "Start: <>", bean, null );
        try
        {
            bean.start();
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                getPlexusLogger( this ).warn( "Error starting: " + Logs.identityToString( bean ), e );
            }
            finally
            {
                Logs.throwUnchecked( e );
            }
        }
    }

    @SuppressWarnings( "finally" )
    private void stop( final Startable bean )
    {
        Logs.debug( "Stop: <>", bean, null );
        try
        {
            bean.stop();
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                getPlexusLogger( this ).warn( "Problem stopping: " + Logs.identityToString( bean ), e );
            }
            finally
            {
                return; // ignore any logging exceptions and continue with shutdown
            }
        }
    }

    @SuppressWarnings( "finally" )
    private void dispose( final Disposable bean )
    {
        Logs.debug( "Dispose: <>", bean, null );
        try
        {
            bean.dispose();
        }
        catch ( final Throwable e )
        {
            Logs.catchThrowable( e );
            try
            {
                getPlexusLogger( this ).warn( "Problem disposing: " + Logs.identityToString( bean ), e );
            }
            finally
            {
                return; // ignore any logging exceptions and continue with shutdown
            }
        }
    }
}
