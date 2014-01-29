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

public abstract class BeanScheduler
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        Object activator;
        try
        {
            activator = new Activator();
        }
        catch ( final LinkageError e )
        {
            activator = null;
        }
        ACTIVATOR = activator;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Object ACTIVATOR;

    static final Object PLACEHOLDER = new Object();

    public static final Module MODULE = new Module()
    {
        public void configure( final Binder binder )
        {
            if ( null != ACTIVATOR )
            {
                binder.bindListener( Matchers.any(), (com.google.inject.spi.ProvisionListener) ACTIVATOR );
            }
        }
    };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ThreadLocal<Object[]> pendingHolder = new ThreadLocal<Object[]>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void schedule( final Object bean )
    {
        if ( null != ACTIVATOR )
        {
            final Object[] holder = getPendingHolder();
            final Object pending = holder[0];
            if ( pending == PLACEHOLDER )
            {
                holder[0] = new Pending( bean );
                return; // will be activated later
            }
            else if ( pending instanceof Pending )
            {
                ( (Pending) pending ).add( bean );
                return; // will be activated later
            }
        }
        activate( bean );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract void activate( final Object bean );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static Object[] getPendingHolder()
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
    private final class Pending
        extends ArrayList<Object>
    {
        Pending( final Object bean )
        {
            add( bean );
        }

        public void activate()
        {
            for ( int i = 0, size = size(); i < size; i++ )
            {
                BeanScheduler.this.activate( get( i ) );
            }
        }
    }

    static final class Activator
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
                if ( pending instanceof Pending )
                {
                    ( (Pending) pending ).activate();
                }
            }
        }
    }
}
