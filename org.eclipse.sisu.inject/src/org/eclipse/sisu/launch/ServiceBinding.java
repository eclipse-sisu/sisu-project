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

import javax.inject.Singleton;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;

final class ServiceBinding<T>
    implements Binding<T>, Provider<T>
{
    private final BundleContext context;

    private final ServiceReference<T> reference;

    private final Key<T> boundKey;

    ServiceBinding( final BundleContext context, final ServiceReference<T> reference, final Key<T> boundKey )
    {
        this.context = context;
        this.reference = reference;
        this.boundKey = boundKey;
    }

    public T get()
    {
        return context.getService( reference );
    }

    @SuppressWarnings( "static-method" )
    public int rank()
    {
        return Integer.MIN_VALUE;
        // return ( (Integer) reference.getProperty( Constants.SERVICE_RANKING ) ).intValue();
    }

    public Key<T> getKey()
    {
        return boundKey;
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
        return visitor.visitScopeAnnotation( Singleton.class );
    }
}
