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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BeanSchedulerTest {
    static class TestScheduler extends BeanScheduler {
        final List<Object> activatedBeans = new ArrayList<>();

        @Override
        protected void activate(final Object bean) {
            activatedBeans.add(bean);
        }
    }

    @Test
    void testSchedulerOneBean() {
        final TestScheduler scheduler = new TestScheduler();

        scheduler.schedule("bean1");
        // without cycle detection, schedule should activate immediately
        assertEquals(1, scheduler.activatedBeans.size());
        assertEquals("bean1", scheduler.activatedBeans.get(0));
    }

    @Test
    void testSchedulerMultipleBeans() {
        final TestScheduler scheduler = new TestScheduler();

        scheduler.schedule("beanA");
        scheduler.schedule("beanB");
        scheduler.schedule("beanC");
        assertEquals(3, scheduler.activatedBeans.size());
    }
}
