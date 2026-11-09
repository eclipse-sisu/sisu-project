/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.bean;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link BeanManager} that manages JSR250 beans and schedules lifecycle events.
 */
public final class LifecycleManager extends BeanScheduler implements BeanManager {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final LifecycleBuilder builder = new LifecycleBuilder();

    private final Map<Class<?>, BeanLifecycle> lifecycles = //
            new ConcurrentHashMap<>(16, 0.75f, 1);

    private final Deque<StoppableBean> stoppableBeans = new ArrayDeque<>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean manage(final Class<?> clazz) {
        return buildLifecycle(clazz);
    }

    @Override
    public PropertyBinding manage(final BeanProperty<?> property) {
        return null; // no custom property bindings
    }

    @Override
    public boolean manage(final Object bean) {
        final BeanLifecycle lifecycle = lifecycleFor(bean);
        if (lifecycle.isStoppable()) {
            pushStoppable(new StoppableBean(bean, lifecycle));
        }
        if (lifecycle.isStartable()) {
            schedule(bean);
        }
        return true;
    }

    @Override
    public boolean unmanage(final Object bean) {
        final StoppableBean stoppable = removeStoppable(bean);
        if (null != stoppable) {
            stoppable.stop();
        }
        return true;
    }

    @Override
    public boolean unmanage() {
        for (StoppableBean stoppable; (stoppable = popStoppable()) != null; ) {
            stoppable.stop();
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void activate(final Object bean) {
        lifecycleFor(bean).start(bean);
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
    private boolean buildLifecycle(final Class<?> clazz) {
        BeanLifecycle lifecycle = lifecycles.get(clazz);
        if (null == lifecycle) {
            lifecycle = builder.build(clazz);
            lifecycles.put(clazz, lifecycle);
        }
        return lifecycle != BeanLifecycle.NO_OP;
    }

    /**
     * Looks up the JSR250 lifecycle previously built for this bean.
     *
     * @param bean The bean instance
     * @return Lifecycle for the bean
     */
    private BeanLifecycle lifecycleFor(final Object bean) {
        if (null != bean) {
            // check the class hierarchy, just in case the bean instance has been proxied/enhanced
            for (Class<?> c = bean.getClass(); null != c && c != Object.class; c = c.getSuperclass()) {
                final BeanLifecycle lifecycle = lifecycles.get(c);
                if (null != lifecycle) {
                    return lifecycle;
                }
            }
        }
        return BeanLifecycle.NO_OP;
    }

    private void pushStoppable(final StoppableBean stoppable) {
        synchronized (stoppableBeans) {
            stoppableBeans.addLast(stoppable);
        }
    }

    private StoppableBean removeStoppable(final Object bean) {
        synchronized (stoppableBeans) {
            for (Iterator<StoppableBean> itr = stoppableBeans.iterator(); itr.hasNext(); ) {
                final StoppableBean stoppable = itr.next();
                if (null != bean && bean.equals(stoppable.bean)) {
                    itr.remove();
                    return stoppable;
                }
            }
            return null;
        }
    }

    private StoppableBean popStoppable() {
        synchronized (stoppableBeans) {
            return stoppableBeans.pollLast();
        }
    }

    /**
     * Flush the cache for each key that satisfies the given predicate
     *
     * @param remove a tester that can decide if this key needs to be flushed or
     *               not.
     * @since 0.9.0.M3
     */
    public void flushCacheFor(ClassTester remove) {
        for (Iterator<Class<?>> iterator = lifecycles.keySet().iterator(); iterator.hasNext(); ) {
            if (remove.shouldFlush(iterator.next())) {
                iterator.remove();
            }
        }
    }

    /**
     * Stoppable bean together with its lifecycle, so it can still be stopped after its type was flushed from the
     * cache.
     */
    private static final class StoppableBean {
        final Object bean;

        private final BeanLifecycle lifecycle;

        StoppableBean(final Object bean, final BeanLifecycle lifecycle) {
            this.bean = bean;
            this.lifecycle = lifecycle;
        }

        void stop() {
            lifecycle.stop(bean);
        }
    }

    /**
     * Allows testing if a class should be flushed from the cache
     *
     * @since 0.9.0.M3
     */
    public static interface ClassTester {

        /**
         * Test if class should be flushed
         *
         * @param clz the class to test
         * @return <code>true</code> if class must be flushed, <code>false</code>
         *         otherwise
         */
        boolean shouldFlush(Class<?> clz);
    }
}
