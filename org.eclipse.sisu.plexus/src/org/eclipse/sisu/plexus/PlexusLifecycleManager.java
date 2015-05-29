/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.security.SecureClassLoader;
import java.util.ArrayDeque;
import java.util.Deque;

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
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.bean.BeanScheduler;
import org.eclipse.sisu.bean.PropertyBinding;
import org.eclipse.sisu.inject.Logs;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * {@link BeanManager} that manages Plexus components requiring lifecycle management.
 */
public final class PlexusLifecycleManager
    extends BeanScheduler
    implements BeanManager, Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Class<?>[] LIFECYCLE_TYPES = { LogEnabled.class, Contextualizable.class, Initializable.class,
        Startable.class, Disposable.class };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Deque<Startable> startableBeans = new ArrayDeque<Startable>();

    private final Deque<Disposable> disposableBeans = new ArrayDeque<Disposable>();

    private final Logger consoleLogger = new ConsoleLogger();

    private final Provider<Context> plexusContextProvider;

    private final Provider<LoggerManager> plexusLoggerManagerProvider;

    private final Provider<?> slf4jLoggerFactoryProvider;

    private final BeanManager delegate;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusLifecycleManager( final Provider<Context> plexusContextProvider,
                                   final Provider<LoggerManager> plexusLoggerManagerProvider,
                                   final Provider<?> slf4jLoggerFactoryProvider, //
                                   final BeanManager delegate )
    {
        this.plexusContextProvider = plexusContextProvider;
        this.plexusLoggerManagerProvider = plexusLoggerManagerProvider;
        this.slf4jLoggerFactoryProvider = slf4jLoggerFactoryProvider;

        this.delegate = delegate;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        BeanScheduler.MODULE.configure( binder );
    }

    public boolean manage( final Class<?> clazz )
    {
        for ( final Class<?> lifecycleType : LIFECYCLE_TYPES )
        {
            if ( lifecycleType.isAssignableFrom( clazz ) )
            {
                return true;
            }
        }
        return null != delegate ? delegate.manage( clazz ) : false;
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
        return null != delegate ? delegate.manage( property ) : null;
    }

    public boolean manage( final Object bean )
    {
        if ( bean instanceof Disposable )
        {
            synchronizedPush( disposableBeans, (Disposable) bean );
        }
        if ( bean instanceof LogEnabled )
        {
            ( (LogEnabled) bean ).enableLogging( getPlexusLogger( bean ) );
        }
        if ( bean instanceof Contextualizable || bean instanceof Initializable || bean instanceof Startable )
        {
            schedule( bean );
        }
        return null != delegate ? delegate.manage( bean ) : true;
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
        return null != delegate ? delegate.unmanage( bean ) : true;
    }

    public boolean unmanage()
    {
        for ( Startable bean; ( bean = synchronizedPop( startableBeans ) ) != null; )
        {
            stop( bean );
        }
        for ( Disposable bean; ( bean = synchronizedPop( disposableBeans ) ) != null; )
        {
            dispose( bean );
        }
        return null != delegate ? delegate.unmanage() : true;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void activate( final Object bean )
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            for ( Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass() )
            {
                // need to check hierarchy in case bean is proxied
                final ClassLoader loader = clazz.getClassLoader();
                if ( loader instanceof SecureClassLoader )
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
                synchronizedPush( startableBeans, startableBean );
                start( startableBean );
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
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

    private static <T> void synchronizedPush( final Deque<T> deque, final T element )
    {
        synchronized ( deque )
        {
            deque.addLast( element );
        }
    }

    private static boolean synchronizedRemove( final Deque<?> deque, final Object element )
    {
        synchronized ( deque )
        {
            return deque.remove( element );
        }
    }

    private static <T> T synchronizedPop( final Deque<T> deque )
    {
        synchronized ( deque )
        {
            return deque.pollLast();
        }
    }

    private void contextualize( final Contextualizable bean )
    {
        Logs.trace( "Contextualize: <>", bean, null );
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
        Logs.trace( "Initialize: <>", bean, null );
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
        Logs.trace( "Start: <>", bean, null );
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
        Logs.trace( "Stop: <>", bean, null );
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
        Logs.trace( "Dispose: <>", bean, null );
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
