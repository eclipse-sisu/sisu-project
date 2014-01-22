/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

/**
 * {@link BeanManager} that manages components requiring lifecycle scheduling.
 */
public abstract class AbstractLifecycleManager
    implements BeanManager, Module
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean hasProvisionListener;
        try
        {
            hasProvisionListener = com.google.inject.spi.ProvisionListener.class.isInterface();
        }
        catch ( final LinkageError e )
        {
            hasProvisionListener = false;
        }
        HAS_PROVISION_LISTENER = hasProvisionListener;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_PROVISION_LISTENER;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ThreadLocal<List<?>[]> pendingHolder = new ThreadLocal<List<?>[]>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        if ( HAS_PROVISION_LISTENER )
        {
            binder.bindListener( Matchers.any(), new LifecycleListener() );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public final void schedule( final Object bean )
    {
        if ( HAS_PROVISION_LISTENER )
        {
            final List<?>[] holder = getPendingHolder();
            List beans = holder[0];
            if ( null == beans || beans.isEmpty() )
            {
                holder[0] = beans = new ArrayList<Object>();
            }
            beans.add( bean );
        }
        else
        {
            activate( bean );
        }
    }

    public boolean unmanage()
    {
        pendingHolder.remove();
        return true;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract void activate( final Object bean );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    List<?>[] getPendingHolder()
    {
        List<?>[] holder = pendingHolder.get();
        if ( null == holder )
        {
            pendingHolder.set( holder = new List[1] );
        }
        return holder;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    final class LifecycleListener
        implements com.google.inject.spi.ProvisionListener
    {
        public <T> void onProvision( final ProvisionInvocation<T> pi )
        {
            final List<?>[] holder = getPendingHolder();
            if ( null == holder[0] )
            {
                List<?> beans;
                holder[0] = Collections.EMPTY_LIST;
                try
                {
                    pi.provision();
                }
                finally
                {
                    beans = holder[0];
                    holder[0] = null;
                }

                for ( int i = 0, size = beans.size(); i < size; i++ )
                {
                    activate( beans.get( i ) );
                }
            }
        }
    }
}
