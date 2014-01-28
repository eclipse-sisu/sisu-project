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

final class BeanStarter
    implements PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method[] methods;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanStarter( final List<Method> methods )
    {
        this.methods = methods.toArray( new Method[methods.size()] );

        // ensure we can invoke all methods
        AccessController.doPrivileged( this );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final Object bean )
    {
        int i = 0;
        try
        {
            for ( ; i < methods.length; i++ )
            {
                methods[i].invoke( bean );
            }
        }
        catch ( final Throwable e )
        {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            Logs.catchThrowable( cause );
            try
            {
                Logs.warn( "Error starting: {}", methods[i], cause );
            }
            finally
            {
                Logs.throwUnchecked( cause );
            }
        }
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        AccessibleObject.setAccessible( methods, true );
        return null;
    }
}
