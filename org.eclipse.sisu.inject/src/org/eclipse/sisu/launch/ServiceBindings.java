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

import com.google.inject.TypeLiteral;

public final class ServiceBindings
    implements BindingPublisher
{
    private final ConcurrentMap<TypeLiteral<?>, BindingTracker<?>> trackers =
        new ConcurrentHashMap<TypeLiteral<?>, BindingTracker<?>>();

    private final BundleContext context;

    private final int maxRank;

    public ServiceBindings( final BundleContext context )
    {
        this( context, Integer.MIN_VALUE );
    }

    public ServiceBindings( final BundleContext context, final int maxRank )
    {
        this.context = context;
        this.maxRank = maxRank;
    }

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
                tracker = oldTracker;
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
