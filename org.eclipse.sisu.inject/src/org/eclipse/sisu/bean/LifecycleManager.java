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
import java.util.List;
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

    private final List<Object> stoppableBeans = new ArrayList<Object>();

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
        if ( null != bean )
        {
            final BeanLifecycle lifecycle = lifecycleFor( bean.getClass() );
            if ( lifecycle.isStoppable() )
            {
                pushStoppable( bean );
            }
            if ( lifecycle.isStartable() )
            {
                schedule( bean );
            }
        }
        return true;
    }

    public boolean unmanage( final Object bean )
    {
        if ( removeStoppable( bean ) )
        {
            lifecycleFor( bean.getClass() ).stop( bean );
        }
        return true;
    }

    public boolean unmanage()
    {
        for ( Object bean; ( bean = popStoppable() ) != null; )
        {
            lifecycleFor( bean.getClass() ).stop( bean );
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void activate( final Object bean )
    {
        lifecycleFor( bean.getClass() ).start( bean );
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
     * Looks up the JSR250 lifecycle built for the given bean type.
     * 
     * @param clazz The bean type
     * @return Lifecycle for the bean
     */
    private BeanLifecycle lifecycleFor( final Class<?> clazz )
    {
        final BeanLifecycle lifecycle = lifecycles.get( clazz );
        return null != lifecycle ? lifecycle : BeanLifecycle.NO_OP;
    }

    private boolean pushStoppable( final Object bean )
    {
        synchronized ( stoppableBeans )
        {
            return stoppableBeans.add( bean );
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
            final int size = stoppableBeans.size();
            return size > 0 ? stoppableBeans.remove( size - 1 ) : null;
        }
    }
}
