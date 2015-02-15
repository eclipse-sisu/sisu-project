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

public final class ServicePublisher
    implements BindingPublisher
{
    private final ConcurrentMap<TypeLiteral<?>, BindingTracker<?>> trackers =
        new ConcurrentHashMap<TypeLiteral<?>, BindingTracker<?>>();

    private final BundleContext context;

    public ServicePublisher( final BundleContext context )
    {
        this.context = context;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final TypeLiteral<T> type = subscriber.type();
        BindingTracker tracker = trackers.get( type );
        if ( null == tracker )
        {
            tracker = new BindingTracker<T>( context, type );
            final BindingTracker oldTracker = trackers.putIfAbsent( type, tracker );
            if ( null != oldTracker )
            {
                tracker = oldTracker;
            }
            else
            {
                tracker.open( true );
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
            tracker.unsubscribe( subscriber ); // TODO: remove tracker when no subscribers?
        }
    }

    public int maxBindingRank()
    {
        return Integer.MIN_VALUE;
    }
}
