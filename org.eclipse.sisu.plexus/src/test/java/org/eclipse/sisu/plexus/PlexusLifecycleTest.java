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
package org.eclipse.sisu.plexus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.eclipse.sisu.PostConstruct;
import org.eclipse.sisu.PreDestroy;
import org.junit.jupiter.api.Test;

public class PlexusLifecycleTest {
    static class PlexusBean implements Startable {
        private final StringBuilder results = new StringBuilder();

        @Override
        public void start() {
            results.append("<");
        }

        @Override
        public void stop() {
            results.append(">");
        }

        @Override
        public String toString() {
            return results.toString();
        }
    }

    static class Jsr250Bean {
        private final StringBuilder results = new StringBuilder();

        @PostConstruct
        public void start() {
            results.append("{");
        }

        @PreDestroy
        public void stop() {
            results.append("}");
        }

        @Override
        public String toString() {
            return results.toString();
        }
    }

    @Test
    public void testPlexusLifecycle() throws Exception {
        // standard Plexus lifecycle is always enabled
        PlexusContainer container = createContainer(false);
        PlexusBean bean = container.lookup(PlexusBean.class);
        assertEquals("<", bean.toString());
        container.dispose();
        assertEquals("<>", bean.toString());

        // same results with JSR250 enabled
        container = createContainer(true);
        bean = container.lookup(PlexusBean.class);
        assertEquals("<", bean.toString());
        container.dispose();
        assertEquals("<>", bean.toString());
    }

    @Test
    public void testJsr250Lifecycle() throws Exception {
        // nothing should happen as JSR250 is off by default
        PlexusContainer container = createContainer(false);
        Jsr250Bean bean = container.lookup(Jsr250Bean.class);
        assertEquals("", bean.toString());
        container.dispose();
        assertEquals("", bean.toString());

        // now try again with JSR250 enabled
        container = createContainer(true);
        bean = container.lookup(Jsr250Bean.class);
        assertEquals("{", bean.toString());
        container.dispose();
        assertEquals("{}", bean.toString());
    }

    private static PlexusContainer createContainer(final boolean jsr250) throws Exception {
        final ContainerConfiguration config = new DefaultContainerConfiguration();
        return new DefaultPlexusContainer(config.setJSR250Lifecycle(jsr250), new AbstractModule() {
            @Override
            protected void configure() {
                bind(PlexusBean.class);
                bind(Jsr250Bean.class);
            }
        });
    }
}
