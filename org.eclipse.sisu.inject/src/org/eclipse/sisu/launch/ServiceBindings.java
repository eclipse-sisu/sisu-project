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
package org.eclipse.sisu.launch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.BindingSubscriber;
import org.osgi.framework.BundleContext;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;

/**
 * Publisher of {@link Binding}s from the OSGi service registry.
 */
public final class ServiceBindings
    implements BindingPublisher
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<TypeLiteral<?>, BindingTracker<?>> trackers =
        new ConcurrentHashMap<TypeLiteral<?>, BindingTracker<?>>();

    private final BundleContext context;

    private final int maxRank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates new publisher of service bindings, using the given OSGi {@link BundleContext} to track services. <br>
     * The published bindings are ranked according to their service ranking (up to the given maximum).
     * 
     * @param context The tracking context
     * @param maxRank Maximum binding rank
     */
    public ServiceBindings( final BundleContext context, final int maxRank )
    {
        this.context = context;
        this.maxRank = maxRank;
    }

    /**
     * Creates new publisher of service bindings, using the given OSGi {@link BundleContext} to track services. <br>
     * The published bindings are given the lowest possible rank so that other bindings take precedent.
     * 
     * @param context The tracking context
     */
    public ServiceBindings( final BundleContext context )
    {
        this( context, Integer.MIN_VALUE );
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final TypeLiteral<T> type = subscriber.type();
        BindingTracker tracker = trackers.get( type );
        if ( null == tracker )
        {
            tracker = new BindingTracker<T>( context, maxRank, type );
            final BindingTracker oldTracker = trackers.putIfAbsent( type, tracker );
            if ( null != oldTracker )
            {
                tracker = oldTracker; // someone got there first, use their tracker
            }
        }
        tracker.subscribe( subscriber );
    }

    public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        @SuppressWarnings( "unchecked" )
        final BindingTracker<T> tracker = (BindingTracker<T>) trackers.get( subscriber.type() );
        if ( null != tracker )
        {
            tracker.unsubscribe( subscriber );
        }
    }

    public int maxBindingRank()
    {
        return maxRank;
    }
}
