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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.eclipse.sisu.inject.Logs;

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

    public boolean isStartable()
    {
        return startMethods.length > 0;
    }

    public boolean isStoppable()
    {
        return stopMethods.length > 0;
    }

    public void start( final Object bean )
    {
        int i = 0;
        try
        {
            for ( ; i < startMethods.length; i++ )
            {
                startMethods[i].invoke( bean );
            }
        }
        catch ( final Throwable e )
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

    @SuppressWarnings( "finally" )
    public void stop( final Object bean )
    {
        for ( int i = stopMethods.length - 1; i >= 0; i-- )
        {
            try
            {
                stopMethods[i].invoke( bean );
            }
            catch ( final Throwable e )
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
