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

    private final List<Class<?>> hierarchy = new ArrayList<Class<?>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized BeanLifecycle build( final Class<?> clazz )
    {
        try
        {
            for ( Class<?> c = clazz; null != c && c != Object.class; c = c.getSuperclass() )
            {
                addLifecycleMethods( c );
            }
            if ( startMethods.isEmpty() && stopMethods.isEmpty() )
            {
                return BeanLifecycle.NO_OP;
            }
            return new BeanLifecycle( startMethods, stopMethods );
        }
        finally
        {
            startMethods.clear();
            stopMethods.clear();
            hierarchy.clear();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void addLifecycleMethods( final Class<?> clazz )
    {
        boolean foundStartMethod = false, foundStopMethod = false;
        for ( final Method m : clazz.getDeclaredMethods() )
        {
            if ( isCandidateMethod( m ) )
            {
                if ( m.isAnnotationPresent( PostConstruct.class ) )
                {
                    foundStartMethod = true;
                    if ( !isOverridden( m ) )
                    {
                        startMethods.add( m );
                    }
                }
                else if ( m.isAnnotationPresent( PreDestroy.class ) )
                {
                    foundStopMethod = true;
                    if ( !isOverridden( m ) )
                    {
                        stopMethods.add( m );
                    }
                }
                if ( foundStartMethod && foundStopMethod )
                {
                    break; // stop once we've seen both annotations
                }
            }
        }
        hierarchy.add( clazz );
    }

    private boolean isOverridden( final Method method )
    {
        final String name = method.getName();
        for ( int i = hierarchy.size() - 1; i >= 0; i-- )
        {
            for ( final Method m : hierarchy.get( i ).getDeclaredMethods() )
            {
                if ( name.equals( m.getName() ) && isCandidateMethod( m ) )
                {
                    final int modifiers = m.getModifiers();
                    if ( Modifier.isPublic( modifiers ) || Modifier.isProtected( modifiers )
                        || ( !Modifier.isPrivate( modifiers ) && samePackage( method, m ) ) )
                    {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    private static boolean isCandidateMethod( final Method method )
    {
        if ( method.getReturnType() == void.class )
        {
            final int modifiers = method.getModifiers();
            if ( !( Modifier.isStatic( modifiers ) || Modifier.isAbstract( modifiers ) || method.isSynthetic() ) )
            {
                return method.getParameterTypes().length == 0;
            }
        }
        return false;
    }

    private static boolean samePackage( final Method lhs, final Method rhs )
    {
        return lhs.getDeclaringClass().getPackage().equals( rhs.getDeclaringClass().getPackage() );
    }
}
