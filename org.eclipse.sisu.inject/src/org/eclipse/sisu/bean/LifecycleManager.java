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
        return hasLifecycle( clazz );
    }

    public PropertyBinding manage( final BeanProperty<?> property )
    {
        return null;
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

    private boolean hasLifecycle( final Class<?> clazz )
    {
        if ( !lifecycles.containsKey( clazz ) )
        {
            lifecycles.put( clazz, builder.build( clazz ) );
        }
        return lifecycles.get( clazz ) != BeanLifecycle.NO_OP;
    }

    private BeanLifecycle lifecycleFor( final Object bean )
    {
        return lifecycleFor( null != bean ? bean.getClass() : null );
    }

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
