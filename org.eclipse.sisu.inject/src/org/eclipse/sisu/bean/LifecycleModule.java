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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public final class LifecycleModule
    implements Module, TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final LifecycleMatcher matcher = new LifecycleMatcher();

    private final Disposer disposer = new Disposer();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        BeanScheduler.MODULE.configure( binder );
        binder.bind( BeanDisposer.class ).toInstance( disposer );
        binder.bindListener( matcher, this );
    }

    public <B> void hear( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final Class<?> clazz = type.getRawType();
        final LifecycleManager manager = manage( clazz );
        disposer.register( clazz, manager );
        encounter.register( manager );
    }

    public LifecycleManager manage( final Class<?> clazz )
    {
        final List<Method> startMethods = matcher.getStartMethods( clazz );
        final List<Method> stopMethods = matcher.getStopMethods( clazz );

        final BeanStarter starter = startMethods.isEmpty() ? null : new BeanStarter( startMethods );
        final BeanStopper stopper = stopMethods.isEmpty() ? null : new BeanStopper( stopMethods );

        return new LifecycleManager( starter, stopper );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    static final class Disposer
        implements BeanDisposer
    {
        private final Map<Class<?>, LifecycleManager> managers = new ConcurrentHashMap<Class<?>, LifecycleManager>();

        public void dispose( final Object bean )
        {
            if ( null != bean )
            {
                final LifecycleManager m = managers.get( bean.getClass() );
                if ( null != m )
                {
                    m.dispose( bean );
                }
            }
        }

        public void dispose()
        {
            for ( final LifecycleManager m : managers.values() )
            {
                m.dispose();
            }
        }

        void register( final Class<?> clazz, final LifecycleManager manager )
        {
            managers.put( clazz, manager );
        }
    }
}
