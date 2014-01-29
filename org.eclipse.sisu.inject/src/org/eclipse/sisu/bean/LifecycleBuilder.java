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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

final class LifecycleBuilder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Method> startMethods = new ArrayList<Method>();

    private final List<Method> stopMethods = new ArrayList<Method>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized BeanLifecycle build( final Class<?> clazz )
    {
        try
        {
            boolean hasLifecycle = false;
            for ( Class<?> c = clazz; null != c && c != Object.class; c = c.getSuperclass() )
            {
                hasLifecycle = addLifecycleMethods( c ) || hasLifecycle;
            }
            return hasLifecycle ? new BeanLifecycle( startMethods, stopMethods ) : BeanLifecycle.NO_OP;
        }
        finally
        {
            startMethods.clear();
            stopMethods.clear();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean addLifecycleMethods( final Class<?> clazz )
    {
        boolean foundStartMethod = false, foundStopMethod = false;
        for ( final Method m : clazz.getDeclaredMethods() )
        {
            if ( m.getReturnType() == void.class )
            {
                final int modifiers = m.getModifiers();
                if ( Modifier.isStatic( modifiers ) || Modifier.isAbstract( modifiers ) || m.isSynthetic() )
                {
                    continue; // lifecycle methods must be non-static and concrete
                }
                else if ( m.isAnnotationPresent( PostConstruct.class ) )
                {
                    startMethods.add( m );
                    foundStartMethod = true;
                }
                else if ( m.isAnnotationPresent( PreDestroy.class ) )
                {
                    stopMethods.add( m );
                    foundStopMethod = true;
                }
                if ( foundStartMethod && foundStopMethod )
                {
                    break; // only one start and/or stop method per declaring class
                }
            }
        }
        return foundStartMethod || foundStopMethod;
    }
}
