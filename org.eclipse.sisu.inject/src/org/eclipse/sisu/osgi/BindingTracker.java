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
package org.eclipse.sisu.osgi;

import java.util.Collection;

import org.eclipse.sisu.inject.BindingSubscriber;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.inject.Weak;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Tracker of {@link ServiceBinding}s from the OSGi service registry.
 */
final class BindingTracker<T>
    extends ServiceTracker<T, ServiceBinding<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Collection<BindingSubscriber<T>> subscribers = Weak.elements();

    private final String clazzName;

    private final int maxRank;

    private boolean isOpen;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BindingTracker( final BundleContext context, final int maxRank, final String clazzName )
    {
        super( context, clazzName, null );
        this.clazzName = clazzName;
        this.maxRank = maxRank;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void subscribe( final BindingSubscriber<T> subscriber )
    {
        synchronized ( subscribers )
        {
            if ( !isOpen )
            {
                open( true );
                Logs.trace( "Started tracking services: {}", filter, null );
                isOpen = true;
            }
            for ( final ServiceBinding<T> binding : getTracked().values() )
            {
                if ( binding.isCompatibleWith( subscriber ) )
                {
                    subscriber.add( binding, binding.rank() );
                }
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
            }
            if ( subscribers.isEmpty() && isOpen )
            {
                close();
                Logs.trace( "Stopped tracking services: {}", filter, null );
                isOpen = false;
            }
        }
    }

    @Override
    public ServiceBinding<T> addingService( final ServiceReference<T> reference )
    {
        final ServiceBinding<T> binding;
        try
        {
            binding = new ServiceBinding<T>( context, clazzName, maxRank, reference );
        }
        catch ( final Exception e )
        {
            Logs.warn( "Problem subscribing to service: {}", reference, e );
            return null;
        }
        synchronized ( subscribers )
        {
            for ( final BindingSubscriber<T> subscriber : subscribers )
            {
                if ( binding.isCompatibleWith( subscriber ) )
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
