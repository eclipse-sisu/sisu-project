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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.eclipse.sisu.inject.Logs;

/**
 * Represents the JSR250 lifecycle for a particular bean type.
 */
final class BeanLifecycle
    implements PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Method[] NO_METHODS = {};

    static final BeanLifecycle NO_OP = new BeanLifecycle( null, null );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method[] startMethods;

    private final Method[] stopMethods;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a new lifecycle based on the given start and stop methods.
     * 
     * @param startMethods The methods used to start the bean; from subclass to superclass
     * @param stopMethods The methods used to stop the bean; from subclass to superclass
     */
    BeanLifecycle( final List<Method> startMethods, final List<Method> stopMethods )
    {
        this.startMethods = toArray( startMethods );
        this.stopMethods = toArray( stopMethods );

        // ensure we can invoke all methods
        AccessController.doPrivileged( this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return {@code true} if this lifecycle can be started; otherwise {@code false}
     */
    public boolean isStartable()
    {
        return startMethods.length > 0;
    }

    /**
     * @return {@code true} if this lifecycle can be stopped; otherwise {@code false}
     */
    public boolean isStoppable()
    {
        return stopMethods.length > 0;
    }

    /**
     * Starts the given bean by invoking the methods defined in this lifecycle.
     * 
     * @param bean The bean to start
     */
    public void start( final Object bean )
    {
        Logs.trace( "PostConstruct: <>", bean, null );

        // start superclass before subclass, bail out at the first failure
        int i = startMethods.length - 1;
        try
        {
            for ( ; i >= 0; i-- )
            {
                startMethods[i].invoke( bean );
            }
        }
        catch ( final Throwable e ) // NOPMD see Logs.catchThrowable
        {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            Logs.catchThrowable( cause );
            try
            {
                Logs.warn( "Error starting: {}", startMethods[i], cause );
            }
            finally
            {
                Logs.throwUnchecked( cause );
            }
        }
    }

    /**
     * Stops the given bean by invoking the methods defined in this lifecycle.
     * 
     * @param bean The bean to stop
     */
    @SuppressWarnings( "finally" )
    public void stop( final Object bean )
    {
        Logs.trace( "PreDestroy: <>", bean, null );

        // stop subclass before superclass, log any failures along the way
        for ( int i = 0; i < stopMethods.length; i++ )
        {
            try
            {
                stopMethods[i].invoke( bean );
            }
            catch ( final Throwable e ) // NOPMD see Logs.catchThrowable
            {
                final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
                Logs.catchThrowable( cause );
                try
                {
                    Logs.warn( "Problem stopping: {}", stopMethods[i], cause );
                }
                finally
                {
                    continue; // ignore any logging exceptions and continue stopping
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        AccessibleObject.setAccessible( startMethods, true );
        AccessibleObject.setAccessible( stopMethods, true );

        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Method[] toArray( final List<Method> methods )
    {
        return null != methods && !methods.isEmpty() ? methods.toArray( new Method[methods.size()] ) : NO_METHODS;
    }
}
