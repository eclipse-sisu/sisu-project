/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

/**
 * Default {@link MutableBeanLocator} that locates qualified beans across a dynamic group of {@link BindingPublisher}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RankedSequence<BindingPublisher> publishers = new RankedSequence<BindingPublisher>();

    private final ConcurrentMap<Long, RankedBindings> cachedBindings = Weak.concurrentValues( 256, 1 );

    // reverse mapping; can't use watcher as key since it may not be unique
    private final Map<WatchedBeans, Object> cachedWatchers = Weak.values();

    private final ImplicitBindings implicitBindings = new ImplicitBindings( publishers );

    private final Long[] typeIdHolder = new Long[1];

    private final ReentrantReadWriteLock publisherLock = new ReentrantReadWriteLock();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<BeanEntry> locate( final Key key )
    {
        final TypeLiteral type = key.getTypeLiteral();
        RankedBindings bindings = fetchBindings( type, null );
        if ( null == bindings )
        {
            synchronized ( cachedBindings ) // perform new lookup
            {
                bindings = fetchBindings( type, typeIdHolder );
                if ( null == bindings )
                {
                    // still not cached, so go ahead with assigned id
                    bindings = new RankedBindings( type, publishers );
                    cachedBindings.put( typeIdHolder[0], bindings );
                }
            }
        }
        final boolean isImplicit = key.getAnnotationType() == null && TypeArguments.isImplicit( type );
        return new LocatedBeans( key, bindings, isImplicit ? implicitBindings : null );
    }

    public void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        publisherLock.readLock().lock();
        try
        {
            // subscribe new watcher to existing publishers while holding the read-lock
            final WatchedBeans beans = new WatchedBeans( key, mediator, watcher );
            for ( final BindingPublisher p : publishers() )
            {
                p.subscribe( beans );
            }
            synchronized ( cachedWatchers )
            {
                cachedWatchers.put( beans, watcher );
            }
        }
        finally
        {
            publisherLock.readLock().unlock();
        }
    }

    public boolean add( final BindingPublisher publisher )
    {
        final WatchedBeans[] currentWatchers;
        publisherLock.writeLock().lock();
        try
        {
            if ( publishers.contains( publisher ) )
            {
                return false;
            }
            Logs.trace( "Add publisher: {}", publisher, null );
            synchronized ( cachedBindings ) // block new lookup while we update the cache
            {
                final int rank = publisher.maxBindingRank();
                publishers.insert( publisher, rank );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.add( publisher, rank );
                }
            }
            synchronized ( cachedWatchers )
            {
                // capture snapshot of current watchers while we hold the write-lock
                currentWatchers = cachedWatchers.keySet().toArray( new WatchedBeans[0] );
            }
            publisherLock.readLock().lock(); // begin downgrade to the read-lock
        }
        finally
        {
            publisherLock.writeLock().unlock();
        }
        try
        {
            // subscribe watchers to the new publisher while holding the read-lock
            for ( final WatchedBeans beans : currentWatchers )
            {
                publisher.subscribe( beans );
            }
        }
        finally
        {
            publisherLock.readLock().unlock();
        }
        return true;
    }

    public boolean remove( final BindingPublisher publisher )
    {
        final BindingPublisher oldPublisher;
        final WatchedBeans[] currentWatchers;
        publisherLock.writeLock().lock();
        try
        {
            synchronized ( cachedBindings ) // block new lookup while we update the cache
            {
                oldPublisher = publishers.remove( publisher );
                if ( null == oldPublisher )
                {
                    return false;
                }
                Logs.trace( "Remove publisher: {}", oldPublisher, null );
                for ( final RankedBindings bindings : cachedBindings.values() )
                {
                    bindings.remove( oldPublisher );
                }
            }
            synchronized ( cachedWatchers )
            {
                // capture snapshot of current watchers while we hold the write-lock
                currentWatchers = cachedWatchers.keySet().toArray( new WatchedBeans[0] );
            }
            publisherLock.readLock().lock(); // begin downgrade to the read-lock
        }
        finally
        {
            publisherLock.writeLock().unlock();
        }
        try
        {
            // unsubscribe watchers from the old publisher while holding the read-lock
            for ( final WatchedBeans beans : currentWatchers )
            {
                oldPublisher.unsubscribe( beans );
            }
        }
        finally
        {
            publisherLock.readLock().unlock();
        }
        // one last round of cleanup in case more was freed
        ( (MildConcurrentValues) cachedBindings ).compact();
        return true;
    }

    public Iterable<BindingPublisher> publishers()
    {
        return publishers.snapshot();
    }

    public void clear()
    {
        for ( final BindingPublisher p : publishers() )
        {
            remove( p );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Fetches any bindings currently associated with the given type.
     * 
     * @param type The generic type
     * @param idReturn Optional holder, returns the assigned type id
     * @return Associated bindings; {@code null} if this is a new type
     */
    @SuppressWarnings( "boxing" )
    private RankedBindings fetchBindings( final TypeLiteral type, final Long[] idReturn )
    {
        // type hash with loader hash is nominally unique, but handle collisions just in case
        final int loaderHash = System.identityHashCode( type.getRawType().getClassLoader() );
        long id = (long) type.hashCode() << 32 | 0x00000000FFFFFFFFL & loaderHash;

        RankedBindings result;
        while ( null != ( result = cachedBindings.get( id ) ) && !type.equals( result.type() ) )
        {
            id++; // collision! (should be very rare) ... resort to linear scan from base id
        }
        if ( null != idReturn )
        {
            idReturn[0] = id;
        }
        return result;
    }

    /**
     * Automatically publishes any {@link Injector} that contains a binding to this {@link BeanLocator}.<br>
     * <br>
     * Relies on Guice's auto-injection, bind the locator with a Provider to disable this feature.
     * 
     * @param injector The injector
     */
    @Inject
    void autoPublish( final Injector injector )
    {
        add( InjectorBindings.findBindingPublisher( injector ) );
    }

    /**
     * Automatically publishes child {@link Injector}s that inherit a binding to this {@link BeanLocator}.<br>
     * <br>
     * Assumes module(s) used to create the child injector request static injection of this class.
     * 
     * @param childInjector The child injector
     */
    @Inject
    static void autoPublishChild( final Injector childInjector )
    {
        // Child injectors cannot use the first 'autoPublish' trick because the locator binding is typically inherited
        // from their parent and the locator instance has already been auto-injected. We workaround this limitation by
        // requesting static injection of this class in a child module, such as ChildWireModule.

        // When the child injector calls us we introspect the locator binding to see if 'autoPublish' would have been
        // called in the parent. We avoid checking the provided instance directly since it may have been deliberately
        // hidden behind a Provider to disable the 'autoPublish' feature, and we need to respect that. Instead we use
        // the Implementations utility to test whether the binding directly exposes this implementation class.

        final Binding<?> locatorBinding = childInjector.getBinding( MutableBeanLocator.class );
        if ( DefaultBeanLocator.class.equals( Implementations.find( locatorBinding ) ) )
        {
            ( (DefaultBeanLocator) locatorBinding.getProvider().get() ).autoPublish( childInjector );
        }
    }
}
