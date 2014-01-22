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

    static final Object PLACEHOLDER = new Object();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ThreadLocal<Object[]> pendingHolder = new ThreadLocal<Object[]>();

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

    public final void schedule( final Object bean )
    {
        if ( HAS_PROVISION_LISTENER )
        {
            final Object[] holder = getPendingHolder();
            final Object pending = holder[0];
            if ( pending == PLACEHOLDER )
            {
                holder[0] = bean; // most common case
            }
            else if ( false == pending instanceof PendingBeans )
            {
                // we have a cycle so upgrade to a sequence
                final PendingBeans beans = new PendingBeans();
                beans.add( pending );
                beans.add( bean );
                holder[0] = beans;
            }
            else
            {
                ( (PendingBeans) pending ).add( bean );
            }
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

    Object[] getPendingHolder()
    {
        Object[] holder = pendingHolder.get();
        if ( null == holder )
        {
            pendingHolder.set( holder = new Object[1] );
        }
        return holder;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    @SuppressWarnings( "serial" )
    static final class PendingBeans
        extends ArrayList<Object>
    {
        // subclass to make it unique
    }

    final class LifecycleListener
        implements com.google.inject.spi.ProvisionListener
    {
        public <T> void onProvision( final ProvisionInvocation<T> pi )
        {
            final Object[] holder = getPendingHolder();
            if ( null == holder[0] )
            {
                final Object pending;
                holder[0] = PLACEHOLDER;
                try
                {
                    pi.provision();
                }
                finally
                {
                    pending = holder[0];
                    holder[0] = null;
                }
                if ( pending != PLACEHOLDER )
                {
                    trigger( pending );
                }
            }
        }

        private void trigger( final Object pending )
        {
            if ( false == pending instanceof PendingBeans )
            {
                activate( pending ); // most common case
            }
            else
            {
                // all beans in the cycle are ready to be activated
                final PendingBeans beans = (PendingBeans) pending;
                for ( int i = 0, size = beans.size(); i < size; i++ )
                {
                    activate( beans.get( i ) );
                }
            }
        }
    }
}
