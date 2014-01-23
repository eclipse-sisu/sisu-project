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
        Object loopDetector;
        try
        {
            loopDetector = new LoopDetector();
        }
        catch ( final LinkageError e )
        {
            loopDetector = null;
        }
        LOOP_DETECTOR = loopDetector;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Object LOOP_DETECTOR;

    static final Object PLACEHOLDER = new Object();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final ThreadLocal<Object[]> pendingHolder = new ThreadLocal<Object[]>();

    private final boolean detectLoops;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    protected AbstractLifecycleManager( final boolean detectLoops )
    {
        this.detectLoops = detectLoops;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        if ( null != LOOP_DETECTOR && detectLoops )
        {
            binder.bindListener( Matchers.any(), (com.google.inject.spi.ProvisionListener) LOOP_DETECTOR );
        }
    }

    public final void schedule( final Object bean )
    {
        if ( null != LOOP_DETECTOR && detectLoops )
        {
            final Object[] holder = getPendingHolder();
            final Object pending = holder[0];
            if ( pending == PLACEHOLDER )
            {
                holder[0] = new PendingBeans( bean );
                return; // will be activated later
            }
            else if ( pending instanceof PendingBeans )
            {
                ( (PendingBeans) pending ).add( bean );
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
    private final class PendingBeans
        extends ArrayList<Object>
    {
        PendingBeans( final Object bean )
        {
            add( bean );
        }

        public void activateGroup()
        {
            for ( int i = 0, size = size(); i < size; i++ )
            {
                activate( get( i ) );
            }
        }
    }

    static final class LoopDetector
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
                if ( pending instanceof PendingBeans )
                {
                    ( (PendingBeans) pending ).activateGroup();
                }
            }
        }
    }
}
