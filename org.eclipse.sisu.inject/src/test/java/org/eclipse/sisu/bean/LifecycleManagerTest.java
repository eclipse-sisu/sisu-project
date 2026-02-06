/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.junit.jupiter.api.Test;

class LifecycleManagerTest {
    static class ManagedBean {
        boolean started;

        boolean stopped;

        @PostConstruct
        void start() {
            started = true;
        }

        @PreDestroy
        void stop() {
            stopped = true;
        }
    }

    static class UnmanagedBean {
        // no lifecycle annotations
    }

    @Test
    void testManageClass() {
        final LifecycleManager manager = new LifecycleManager();
        assertTrue(manager.manage(ManagedBean.class));
        assertFalse(manager.manage(UnmanagedBean.class));
    }

    @Test
    void testManageProperty() {
        final LifecycleManager manager = new LifecycleManager();
        assertNull(manager.manage((BeanProperty<?>) null));
    }

    @Test
    void testManageAndActivateBean() {
        final LifecycleManager manager = new LifecycleManager();
        manager.manage(ManagedBean.class);

        final ManagedBean bean = new ManagedBean();
        assertTrue(manager.manage(bean));
        assertTrue(bean.started);
    }

    @Test
    void testUnmanageSpecificBean() {
        final LifecycleManager manager = new LifecycleManager();
        manager.manage(ManagedBean.class);

        final ManagedBean bean = new ManagedBean();
        manager.manage(bean);

        assertTrue(manager.unmanage(bean));
        assertTrue(bean.stopped);
    }

    @Test
    void testUnmanageAll() {
        final LifecycleManager manager = new LifecycleManager();
        manager.manage(ManagedBean.class);

        final ManagedBean beanA = new ManagedBean();
        final ManagedBean beanB = new ManagedBean();
        manager.manage(beanA);
        manager.manage(beanB);

        assertTrue(manager.unmanage());
        assertTrue(beanA.stopped);
        assertTrue(beanB.stopped);
    }

    @Test
    void testUnmanagedBeanDoesNotFail() {
        final LifecycleManager manager = new LifecycleManager();
        final UnmanagedBean bean = new UnmanagedBean();
        assertTrue(manager.manage(bean));
        assertTrue(manager.unmanage(bean));
    }

    @Test
    void testFlushCacheFor() {
        final LifecycleManager manager = new LifecycleManager();
        manager.manage(ManagedBean.class);
        manager.manage(UnmanagedBean.class);

        manager.flushCacheFor(new LifecycleManager.ClassTester() {
            @Override
            public boolean shouldFlush(final Class<?> clz) {
                return clz == ManagedBean.class;
            }
        });

        // After flushing, re-managing should re-build the lifecycle
        assertTrue(manager.manage(ManagedBean.class));
    }
}
