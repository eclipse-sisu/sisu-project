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
            openIfNecessary();
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
            closeIfNecessary();
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
            closeIfNecessary();
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
            closeIfNecessary();
        }
        super.removedService( reference, binding );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void openIfNecessary()
    {
        if ( !isOpen )
        {
            open( true ); // calls addingService to pre-fill the tracker
            Logs.trace( "Started tracking services: {}", filter, null );
            isOpen = true; // set last to avoid premature close
        }
    }

    private void closeIfNecessary()
    {
        if ( isOpen && subscribers.isEmpty() )
        {
            isOpen = false; // set first to avoid repeated close
            Logs.trace( "Stopped tracking services: {}", filter, null );
            close(); // calls removedService to clear out the tracker
        }
    }
}
