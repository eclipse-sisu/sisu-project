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

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;

/**
 * Service {@link Binding} backed by an OSGi {@link ServiceReference}.
 */
final class ServiceBinding<T>
    implements Binding<T>, Provider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BundleContext context;

    private final int maxRank;

    private final ServiceReference<T> reference;

    private final Key<T> serviceKey;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ServiceBinding( final BundleContext context, final int maxRank, final TypeLiteral<T> type,
                    final ServiceReference<T> reference )
    {
        this.context = context;
        this.maxRank = maxRank;
        this.reference = reference;

        final Object name = reference.getProperty( "name" );
        if ( name instanceof String && ( (String) name ).length() > 0 )
        {
            serviceKey = Key.get( type, Names.named( (String) name ) );
        }
        else
        {
            serviceKey = Key.get( type );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public T get()
    {
        return context.getService( reference );
    }

    public int rank()
    {
        if ( maxRank > Integer.MIN_VALUE )
        {
            // limit the exposed rank to the given maximum
            final int serviceRank = ( (Number) reference.getProperty( Constants.SERVICE_RANKING ) ).intValue();
            if ( serviceRank < maxRank )
            {
                return serviceRank;
            }
        }
        return maxRank;
    }

    public Key<T> getKey()
    {
        return serviceKey;
    }

    public Provider<T> getProvider()
    {
        return this;
    }

    public Object getSource()
    {
        return reference;
    }

    public void applyTo( final Binder binder )
    {
        // no-op
    }

    public <V> V acceptVisitor( final ElementVisitor<V> visitor )
    {
        return visitor.visit( this );
    }

    public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super T, V> visitor )
    {
        return null;
    }

    public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
    {
        return visitor.visitNoScoping();
    }
}
