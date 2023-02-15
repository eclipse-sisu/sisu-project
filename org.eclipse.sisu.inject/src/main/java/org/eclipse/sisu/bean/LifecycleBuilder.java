/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sisu.PostConstruct;
import org.eclipse.sisu.PreDestroy;

/**
 * Builds {@link BeanLifecycle}s by searching class hierarchies for JSR250 annotations.
 */
final class LifecycleBuilder
{
    static
    {
        boolean hasJsr250Lifecycle;
        try
        {
            hasJsr250Lifecycle = javax.annotation.PostConstruct.class.isAnnotation()
                    && javax.annotation.PreDestroy.class.isAnnotation();
        }
        catch ( final LinkageError e )
        {
            hasJsr250Lifecycle = false;
        }
        HAS_JSR250_LIFECYCLE = hasJsr250Lifecycle;
    }

    private static final boolean HAS_JSR250_LIFECYCLE;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Method> startMethods = new ArrayList<Method>();

    private final List<Method> stopMethods = new ArrayList<Method>();

    private final List<Class<?>> hierarchy = new ArrayList<Class<?>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Builds a new {@link BeanLifecycle} for the given bean type.
     * 
     * @param clazz The bean type
     * @return Lifecycle for the bean
     */
    public synchronized BeanLifecycle build( final Class<?> clazz )
    {
        try
        {
            // process subclass methods before superclass
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

    /**
     * Adds any declared {@link PostConstruct} and {@link PreDestroy} methods to the current lifecycle.<br>
     * Ignores methods overridden in subclasses, as well as multiple declarations of each annotation.
     * 
     * @param clazz
     */
    private void addLifecycleMethods( final Class<?> clazz )
    {
        boolean foundStartMethod = false, foundStopMethod = false;
        for ( final Method m : clazz.getDeclaredMethods() )
        {
            if ( isCandidateMethod( m ) )
            {
                if ( isAnnotationPresent( m, PostConstruct.class ) )
                {
                    foundStartMethod = true;
                    if ( !isOverridden( m ) )
                    {
                        startMethods.add( m );
                    }
                }
                else if ( isAnnotationPresent( m, PreDestroy.class ) )
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

    private boolean isAnnotationPresent( final Method method, final Class<? extends Annotation> annotationClass )
    {
        boolean result = method.isAnnotationPresent( annotationClass );
        if ( !result && HAS_JSR250_LIFECYCLE )
        {
            if ( PostConstruct.class.equals( annotationClass ) )
            {
                result = method.isAnnotationPresent( javax.annotation.PostConstruct.class );
            }
            else if ( PreDestroy.class.equals( annotationClass ) )
            {
                result = method.isAnnotationPresent( javax.annotation.PreDestroy.class );
            }
        }
        return result;
    }

    /**
     * Tests to see if the given method is overridden in the subclass hierarchy.
     * 
     * @param method The method to test
     * @return {@code true} if the method was overridden; otherwise {@code false}
     */
    private boolean isOverridden( final Method method )
    {
        // walk back down the cached hierarchy
        final String name = method.getName();
        for ( int i = hierarchy.size() - 1; i >= 0; i-- )
        {
            for ( final Method m : hierarchy.get( i ).getDeclaredMethods() )
            {
                // method with same name, void return, and no parameters
                if ( name.equals( m.getName() ) && isCandidateMethod( m ) )
                {
                    final int modifiers = m.getModifiers();
                    if ( Modifier.isPublic( modifiers ) || Modifier.isProtected( modifiers )
                        || ( !Modifier.isPrivate( modifiers ) && samePackage( method, m ) ) )
                    {
                        return true;
                    }
                    break; // can't have two candidates in same class, so proceed to subclass
                }
            }
        }
        return false;
    }

    /**
     * Tests to see if this method is a lifecycle candidate: void return, not static/abstract, no parameters.
     * 
     * @param method The method to test
     * @return {@code true} if the method is acceptable; otherwise {@code false}
     */
    private static boolean isCandidateMethod( final Method method )
    {
        // order tests by performance/result ratio
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

    /**
     * @return {@code true} if the methods were declared in the same package; otherwise {@code false}
     */
    private static boolean samePackage( final Method lhs, final Method rhs )
    {
        return lhs.getDeclaringClass().getPackage().equals( rhs.getDeclaringClass().getPackage() );
    }
}
