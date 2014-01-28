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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

final class LifecycleMatcher
    extends AbstractMatcher<TypeLiteral<?>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Class<?>, Boolean> results = new HashMap<Class<?>, Boolean>();

    private final Map<Class<?>, Method> startMethods = new HashMap<Class<?>, Method>();

    private final Map<Class<?>, Method> stopMethods = new HashMap<Class<?>, Method>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean matches( final TypeLiteral<?> type )
    {
        return matches( type.getRawType() );
    }

    public synchronized boolean matches( final Class<?> clazz )
    {
        if ( null == clazz || clazz == Object.class )
        {
            return false;
        }
        Boolean hasLifecycle = results.get( clazz );
        if ( null == hasLifecycle )
        {
            final boolean declaresLifecycle = declaresLifecycleMethod( clazz );
            hasLifecycle = Boolean.valueOf( declaresLifecycle || matches( clazz.getSuperclass() ) );
            results.put( clazz, hasLifecycle );
        }
        return hasLifecycle.booleanValue();
    }

    public synchronized List<Method> getStartMethods( final Class<?> clazz )
    {
        return filterMethods( startMethods, clazz );
    }

    public synchronized List<Method> getStopMethods( final Class<?> clazz )
    {
        return filterMethods( stopMethods, clazz );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static boolean isStartMethod( final Method method )
    {
        return method.isAnnotationPresent( PostConstruct.class );
    }

    private static boolean isStopMethod( final Method method )
    {
        return method.isAnnotationPresent( PreDestroy.class );
    }

    private static List<Method> filterMethods( final Map<Class<?>, Method> methods, final Class<?> clazz )
    {
        final List<Method> result = new ArrayList<Method>();
        for ( Class<?> c = clazz; null != c && c != Object.class; c = c.getSuperclass() )
        {
            final Method m = methods.get( c );
            if ( null != m )
            {
                result.add( m );
            }
        }
        return result;
    }

    private boolean declaresLifecycleMethod( final Class<?> clazz )
    {
        boolean foundStartMethod = false, foundStopMethod = false;
        for ( final Method m : clazz.getDeclaredMethods() )
        {
            if ( m.getReturnType() == Void.class )
            {
                final int modifiers = m.getModifiers();
                if ( Modifier.isStatic( modifiers ) || Modifier.isAbstract( modifiers ) || m.isSynthetic() )
                {
                    continue; // lifecycle methods must be non-static and concrete
                }
                else if ( isStartMethod( m ) )
                {
                    startMethods.put( clazz, m );
                    foundStartMethod = true;
                }
                else if ( isStopMethod( m ) )
                {
                    stopMethods.put( clazz, m );
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
