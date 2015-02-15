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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sisu.inject.BindingSubscriber;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

final class BindingTracker<T>
    extends ServiceTracker<T, ServiceBinding<T>>
{
    private final Set<BindingSubscriber<T>> subscribers = new HashSet<BindingSubscriber<T>>();

    private final Bundle definingBundle;

    private final Key<T> boundKey;

    BindingTracker( final BundleContext context, final TypeLiteral<T> type )
    {
        super( context, type.getRawType().getName(), null );
        definingBundle = FrameworkUtil.getBundle( type.getRawType() );
        boundKey = Key.get( type );
    }

    public synchronized void subscribe( final BindingSubscriber<T> subscriber )
    {
        for ( final ServiceBinding<T> binding : getTracked().values() )
        {
            subscriber.add( binding, binding.rank() );
        }
        subscribers.add( subscriber );
    }

    public synchronized void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        for ( final ServiceBinding<T> binding : getTracked().values() )
        {
            subscriber.remove( binding );
        }
        subscribers.remove( subscriber );
    }

    @Override
    public synchronized ServiceBinding<T> addingService( final ServiceReference<T> reference )
    {
        ServiceBinding<T> binding = null;
        final String clazzName = boundKey.getTypeLiteral().getRawType().getName();
        if ( null == definingBundle || reference.isAssignableTo( definingBundle, clazzName ) )
        {
            binding = new ServiceBinding<T>( context, reference, boundKey );
            for ( final BindingSubscriber<T> subscriber : subscribers )
            {
                subscriber.add( binding, binding.rank() );
            }
        }
        return binding;
    }

    @Override
    public synchronized void removedService( final ServiceReference<T> reference, final ServiceBinding<T> binding )
    {
        for ( final BindingSubscriber<T> subscriber : subscribers )
        {
            subscriber.remove( binding );
        }
    }
}
