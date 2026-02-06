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
package org.eclipse.sisu.space;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.Guice;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.junit.jupiter.api.Test;

class QualifiedProviderTest {
    @Named("jsr330-counting")
    static class JSR330CountingThreadProvider implements javax.inject.Provider<Thread> {
        @Inject
        @Named("counting")
        Runnable runnable;

        static int count;

        @Override
        public Thread get() {
            count++;
            return new Thread(runnable);
        }
    }

    @Named("guice-counting")
    static class GuiceCountingThreadProvider implements com.google.inject.Provider<Thread> {
        @Inject
        @Named("counting")
        Runnable runnable;

        static int count;

        @Override
        public Thread get() {
            count++;
            return new Thread(runnable);
        }
    }

    @Named("counting")
    @Typed
    static class CountingRunnable implements Runnable {
        static final AtomicInteger count = new AtomicInteger();

        @Override
        public void run() {
            count.incrementAndGet();
        }
    }

    @Inject
    @Named("jsr330-counting")
    Provider<Thread> jsr330ThreadProvider;

    @Inject
    @Named("guice-counting")
    Provider<Thread> guiceThreadProvider;

    @Test
    void testQualifiedProvider() {
        final ClassSpace space = new URLClassSpace(
                getClass().getClassLoader(), new URL[] {getClass().getResource("")});
        Guice.createInjector(new SpaceModule(space)).injectMembers(this);

        final Thread[] ts = new Thread[8];

        assertEquals(0, JSR330CountingThreadProvider.count);
        assertEquals(0, GuiceCountingThreadProvider.count);

        for (int i = 0; i < ts.length; i++) {
            ts[i] = i % 2 == 0 ? jsr330ThreadProvider.get() : guiceThreadProvider.get();
        }

        assertEquals(4, JSR330CountingThreadProvider.count);
        assertEquals(4, GuiceCountingThreadProvider.count);

        assertEquals(0, CountingRunnable.count.get());

        for (final Thread t : ts) {
            t.start();
        }

        for (final Thread t : ts) {
            try {
                t.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(8, CountingRunnable.count.get());
    }
}
