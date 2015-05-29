/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link BeanManager} that manages JSR250 beans and schedules lifecycle events.
 */
public final class LifecycleManager
    extends BeanScheduler
    implements BeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final LifecycleBuilder builder = new LifecycleBuilder();

    private final Map<Class<?>, BeanLifecycle> lifecycles = new ConcurrentHashMap<Class<?>, BeanLifecycle>();

    private final Deque<Object> stoppableBeans = new ArrayDeque<Object>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Class<?> clazz )
    {
        return buildLifecycle( clazz );
    }

    public PropertyBinding manage( final BeanProperty<?> property )
    {
        return null; // no custom property bindings
    }

    public boolean manage( final Object bean )
    {
        final BeanLifecycle lifecycle = lifecycleFor( bean );
        if ( lifecycle.isStoppable() )
        {
            pushStoppable( bean );
        }
        if ( lifecycle.isStartable() )
        {
            schedule( bean );
        }
        return true;
    }

    public boolean unmanage( final Object bean )
    {
        if ( removeStoppable( bean ) )
        {
            lifecycleFor( bean ).stop( bean );
        }
        return true;
    }

    public boolean unmanage()
    {
        for ( Object bean; ( bean = popStoppable() ) != null; )
        {
            lifecycleFor( bean ).stop( bean );
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void activate( final Object bean )
    {
        lifecycleFor( bean ).start( bean );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to build a JSR250 lifecycle for the given bean type.
     * 
     * @param clazz The bean type
     * @return {@code true} if the bean defines a lifecycle; otherwise {@code false}
     */
    private boolean buildLifecycle( final Class<?> clazz )
    {
        BeanLifecycle lifecycle = lifecycles.get( clazz );
        if ( null == lifecycle )
        {
            lifecycle = builder.build( clazz );
            lifecycles.put( clazz, lifecycle );
        }
        return lifecycle != BeanLifecycle.NO_OP;
    }

    /**
     * Looks up the JSR250 lifecycle previously built for this bean.
     * 
     * @param bean The bean instance
     * @return Lifecycle for the bean
     */
    private BeanLifecycle lifecycleFor( final Object bean )
    {
        if ( null != bean )
        {
            // check the class hierarchy, just in case the bean instance has been proxied/enhanced
            for ( Class<?> c = bean.getClass(); null != c && c != Object.class; c = c.getSuperclass() )
            {
                final BeanLifecycle lifecycle = lifecycles.get( c );
                if ( null != lifecycle )
                {
                    return lifecycle;
                }
            }
        }
        return BeanLifecycle.NO_OP;
    }

    private void pushStoppable( final Object bean )
    {
        synchronized ( stoppableBeans )
        {
            stoppableBeans.addLast( bean );
        }
    }

    private boolean removeStoppable( final Object bean )
    {
        synchronized ( stoppableBeans )
        {
            return stoppableBeans.remove( bean );
        }
    }

    private Object popStoppable()
    {
        synchronized ( stoppableBeans )
        {
            return stoppableBeans.pollLast();
        }
    }
}
