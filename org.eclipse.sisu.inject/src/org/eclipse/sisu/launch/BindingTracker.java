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

import com.google.inject.TypeLiteral;

final class BindingTracker<T>
    extends ServiceTracker<T, ServiceBinding<T>>
{
    private final Set<BindingSubscriber<T>> subscribers = new HashSet<BindingSubscriber<T>>();

    private final int maxRank;

    private final TypeLiteral<T> type;

    private final Bundle definingBundle;

    BindingTracker( final BundleContext context, final int maxRank, final TypeLiteral<T> type )
    {
        super( context, type.getRawType().getName(), null );

        this.maxRank = maxRank;
        this.type = type;

        definingBundle = FrameworkUtil.getBundle( type.getRawType() );
    }

    public void subscribe( final BindingSubscriber<T> subscriber )
    {
        synchronized ( subscribers )
        {
            if ( subscribers.isEmpty() )
            {
                open( true );
            }
            for ( final ServiceBinding<T> binding : getTracked().values() )
            {
                subscriber.add( binding, binding.rank() );
            }
            subscribers.add( subscriber );
        }
    }

    public void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        synchronized ( subscribers )
        {
            if ( subscribers.remove( subscriber ) )
            {
                for ( final ServiceBinding<T> binding : getTracked().values() )
                {
                    subscriber.remove( binding );
                }
                if ( subscribers.isEmpty() )
                {
                    close();
                }
            }
        }
    }

    @Override
    public ServiceBinding<T> addingService( final ServiceReference<T> reference )
    {
        ServiceBinding<T> binding = null;
        final String clazzName = type.getRawType().getName();
        if ( null == definingBundle || reference.isAssignableTo( definingBundle, clazzName ) )
        {
            binding = new ServiceBinding<T>( context, maxRank, type, reference );
            synchronized ( subscribers )
            {
                for ( final BindingSubscriber<T> subscriber : subscribers )
                {
                    subscriber.add( binding, binding.rank() );
                }
            }
        }
        return binding;
    }

    @Override
    public void removedService( final ServiceReference<T> reference, final ServiceBinding<T> binding )
    {
        synchronized ( subscribers )
        {
            for ( final BindingSubscriber<T> subscriber : subscribers )
            {
                subscriber.remove( binding );
            }
        }
        super.removedService( reference, binding );
    }
}
