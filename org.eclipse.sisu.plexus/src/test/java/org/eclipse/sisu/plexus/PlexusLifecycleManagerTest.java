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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.inject.AbstractModule;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This UT tests scenario, where legacy (Plexus) meets modern (JSR330) components. Assume you have a component that
 * originally was a Plexus component (and Plexus XML still exists somewhere), but the class was migrated to JSR330.
 * Most specifically, the class used Plexus Logger as requirement and now is migrated to JSR330 and uses Slf4j
 * logger factory, hence, the logger field became {@code final}. In this single, very special case, Plexus Shim
 * my opt to not inject the value, given field is final, and we already created bean instance, and it implies
 * that field was already populated.
 * This prevents unwanted warnings on Java 26+ where final fields are being modified by Sisu.
 */
class PlexusLifecycleManagerTest {
    @Component(role = Object.class)
    static class LoggingBean {
        @Requirement
        private Logger logger;
    }

    @Component(role = Object.class)
    static class OtherLoggingBean {
        @Requirement
        private final Logger logger = LoggerFactory.getLogger("other-logging-bean");
    }

    @Test
    void testLoggers() throws Exception {
        PlexusContainer container = createContainer();
        try {
            LoggingBean loggingBean = container.lookup(LoggingBean.class);
            assertNotNull(loggingBean.logger);
            assertEquals(LoggingBean.class.getName(), loggingBean.logger.getName());

            OtherLoggingBean otherLoggingBean = container.lookup(OtherLoggingBean.class);
            assertNotNull(otherLoggingBean.logger);
            assertEquals("other-logging-bean", otherLoggingBean.logger.getName());
        } finally {
            container.dispose();
        }
    }

    private static PlexusContainer createContainer() throws Exception {
        final ContainerConfiguration config = new DefaultContainerConfiguration();
        return new DefaultPlexusContainer(config, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LoggingBean.class);
                bind(OtherLoggingBean.class);
            }
        });
    }
}
