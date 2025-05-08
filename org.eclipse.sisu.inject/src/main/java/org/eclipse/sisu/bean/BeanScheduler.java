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
package org.eclipse.sisu.bean;

import java.util.ArrayList;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.DefaultBindingScopingVisitor;

/**
 * Schedules safe activation of beans even when cyclic dependencies are involved.<br>
 * Takes advantage of the new Guice ProvisionListener SPI, if available at runtime.
 */
public abstract class BeanScheduler
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        Object cycleActivator;
        Object candidateCycle = new Object();
        Object cycleConfirmed = new Object();
        try
        {
            // extra check in case we have both old and new versions of guice overlapping on the runtime classpath
            Binder.class.getMethod( "bindListener", Matcher.class, com.google.inject.spi.ProvisionListener[].class );

            // allow cycle detection to be turned off completely
            final String detectCycles = System.getProperty( "sisu.detect.cycles" );
            if ( "false".equalsIgnoreCase( detectCycles ) )
            {
                cycleActivator = null;
            }
            else
            {
                cycleActivator = new CycleActivator();
            }

            // support use of the old 'pessimistic' approach
            if ( "pessimistic".equalsIgnoreCase( detectCycles ) )
            {
                candidateCycle = cycleConfirmed;
            }
        }
        catch ( final Exception e )
        {
            cycleActivator = null;
        }
        catch ( final LinkageError e )
        {
            cycleActivator = null;
        }
        CYCLE_ACTIVATOR = cycleActivator;
        CANDIDATE_CYCLE = candidateCycle;
        CYCLE_CONFIRMED = cycleConfirmed;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Object CYCLE_ACTIVATOR;

    static final Object CANDIDATE_CYCLE;

    static final Object CYCLE_CONFIRMED;

    /**
     * Enables deferred activation of component cycles, only needed in legacy systems like Plexus.
     */
    public static final Module MODULE = new Module()
    {
        public void configure( final Binder binder )
        {
            if ( null != CYCLE_ACTIVATOR )
            {
                binder.bindListener( Matchers.any(), (com.google.inject.spi.ProvisionListener) CYCLE_ACTIVATOR );
            }
        }
    };

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    static final ThreadLocal<Object[]> pendingHolder = new ThreadLocal<Object[]>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Detects if a dependency cycle exists and activation needs to be deferred.
     */
    public static void detectCycle( final Object value )
    {
        if ( null != CYCLE_ACTIVATOR && Scopes.isCircularProxy( value ) )
        {
            final Object[] holder = pendingHolder.get();
            if ( null != holder )
            {
                final Object pending = holder[0];
                if ( CANDIDATE_CYCLE.equals( pending ) )
                {
                    holder[0] = CYCLE_CONFIRMED;
                }
            }
        }
    }

    /**
     * Schedules activation of the given bean at the next safe activation point.
     * 
     * @param bean The managed bean
     */
    public final void schedule( final Object bean )
    {
        if ( null != CYCLE_ACTIVATOR )
        {
            final Object[] holder = pendingHolder.get();
            if ( null != holder )
            {
                final Object pending = holder[0];
                if ( CYCLE_CONFIRMED.equals( pending ) )
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
        }
        activate( bean ); // no ProvisionListener, so activate immediately
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    /**
     * Customized activation of the given bean.
     * 
     * @param bean The bean to activate
     */
    protected abstract void activate( final Object bean );

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Collects pending beans waiting for activation.
     */
    @SuppressWarnings( "serial" )
    private final class Pending
        extends ArrayList<Object>
    {
        Pending( final Object bean )
        {
            add( bean );
        }

        /**
         * Activates pending beans in order of registration, that is in the order they finished injection.
         */
        public void activate()
        {
            for ( int i = 0, size = size(); i < size; i++ )
            {
                BeanScheduler.this.activate( get( i ) );
            }
        }
    }

    /**
     * Listens to provisioning events in order to determine safe activation points.
     */
    static final class CycleActivator
        implements com.google.inject.spi.ProvisionListener
    {
        private static final BindingScopingVisitor<Boolean> IS_SCOPED = new DefaultBindingScopingVisitor<Boolean>()
        {
            @Override
            public Boolean visitNoScoping()
            {
                return Boolean.FALSE;
            }

            @Override
            protected Boolean visitOther()
            {
                return Boolean.TRUE;
            }
        };

        public <T> void onProvision( final ProvisionInvocation<T> pi )
        {
            // Only scoped dependencies like singletons are candidates for dependency cycles
            if ( Boolean.TRUE.equals( pi.getBinding().acceptScopingVisitor( IS_SCOPED ) ) )
            {
                Object[] holder = pendingHolder.get();
                if ( null == holder )
                {
                    pendingHolder.set( holder = new Object[1] );
                }
                if ( null == holder[0] )
                {
                    final Object pending;
                    holder[0] = CANDIDATE_CYCLE;
                    try
                    {
                        pi.provision(); // may involve nested calls/cycles
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
}
