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
package org.eclipse.sisu.locators;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.locators.spi.BindingPublisher;
import org.eclipse.sisu.reflect.Logs;
import org.eclipse.sisu.reflect.Soft;
import org.eclipse.sisu.reflect.TypeParameters;
import org.eclipse.sisu.reflect.Weak;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Default {@link MutableBeanLocator} that locates qualified beans across a dynamic group of {@link BindingPublisher}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class DefaultBeanLocator
    extends ReentrantLock
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RankedSequence<BindingPublisher> publishers = new RankedSequence<BindingPublisher>();

    private final ConcurrentMap<TypeLiteral, RankedBindings> cachedBindings = Soft.concurrentValues( 256, 8 );

    // reverse mapping; can't use watcher as key since it may not be unique
    private final Map<WatchedBeans, Object> cachedWatchers = Weak.values();

    private final ImplicitBindings implicitBindings = new ImplicitBindings( publishers );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<BeanEntry> locate( final Key key )
    {
        final TypeLiteral type = key.getTypeLiteral();
        RankedBindings bindings = cachedBindings.get( type );
        if ( null == bindings )
        {
            lock();
            try
            {
                bindings = new RankedBindings( type, publishers );
                final RankedBindings oldBindings = cachedBindings.putIfAbsent( type, bindings );
                if ( null != oldBindings )
                {
                    bindings = oldBindings;
                }
            }
            finally
            {
                unlock();
            }
        }
        final boolean isImplicit = key.getAnnotationType() == null && TypeParameters.isImplicit( type );
        return new LocatedBeans( key, bindings, isImplicit ? implicitBindings : null );
    }

    public void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        lock();
        try
        {
            final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                p.subscribe( beans );
            }
            cachedWatchers.put( beans, watcher );
        }
        finally
        {
            unlock();
        }
    }

    public void add( final BindingPublisher publisher, final int rank )
    {
        lock();
        try
        {
            if ( !publishers.contains( publisher ) )
            {
                Logs.debug( "Add publisher: {}", publisher, null );
                publishers.insert( publisher, rank );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.add( publisher, rank );
                }
                for ( final WatchedBeans beans : cachedWatchers.keySet() )
                {
                    publisher.subscribe( beans );
                }
            }
        }
        finally
        {
            unlock();
        }
    }

    public void remove( final BindingPublisher publisher )
    {
        lock();
        try
        {
            if ( publishers.remove( publisher ) )
            {
                Logs.debug( "Remove publisher: {}", publisher, null );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.remove( publisher );
                }
                for ( final WatchedBeans beans : cachedWatchers.keySet() )
                {
                    publisher.unsubscribe( beans );
                }
            }
        }
        finally
        {
            unlock();
        }
    }

    public void clear()
    {
        lock();
        try
        {
            for ( final BindingPublisher p : publishers.snapshot() )
            {
                remove( p );
            }
        }
        finally
        {
            unlock();
        }
    }

    public void add( final Injector injector, final int rank )
    {
        add( new InjectorPublisher( injector, new DefaultRankingFunction( rank ) ), rank );
    }

    public void remove( final Injector injector )
    {
        remove( new InjectorPublisher( injector, null ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Automatically publishes any {@link Injector} that contains a binding to this {@link BeanLocator}.
     * 
     * @param injector The injector
     */
    @Inject
    void autoPublish( final Injector injector )
    {
        staticAutoPublish( this, injector );
    }

    @Inject
    static void staticAutoPublish( final MutableBeanLocator locator, final Injector injector )
    {
        final RankingFunction function = injector.getInstance( RankingFunction.class );
        locator.add( new InjectorPublisher( injector, function ), function.maxRank() );
    }
}
