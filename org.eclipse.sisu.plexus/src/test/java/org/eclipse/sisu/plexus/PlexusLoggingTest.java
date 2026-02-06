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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import javax.inject.Inject;
import junit.framework.TestCase;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.bean.PropertyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlexusLoggingTest extends TestCase {
    static class LoggerManager implements BeanManager {
        @Override
        public boolean manage(final Class<?> clazz) {
            return false;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public PropertyBinding manage(final BeanProperty property) {
            if (Logger.class.equals(property.getType().getRawType())) {
                return new PropertyBinding() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <B> void injectProperty(final B bean) {
                        property.set(bean, LoggerFactory.getLogger(bean.getClass()));
                    }
                };
            }
            return null;
        }

        @Override
        public boolean manage(final Object bean) {
            return false;
        }

        @Override
        public boolean unmanage(final Object bean) {
            return false;
        }

        @Override
        public boolean unmanage() {
            return false;
        }
    }

    @Override
    protected void setUp() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new PlexusDateTypeConverter());

                bind(PlexusBeanLocator.class).to(DefaultPlexusBeanLocator.class);
                bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);

                install(new PlexusBindingModule(new LoggerManager(), new PlexusAnnotatedBeanModule(null, null)));

                requestInjection(PlexusLoggingTest.this);
            }
        });
    }

    @Component(role = Object.class)
    static class SomeComponent {
        @Requirement
        Logger logger;
    }

    @Inject
    SomeComponent component;

    public void testLogging() {
        assertNotNull(component.logger);

        assertEquals(SomeComponent.class.getName(), component.logger.getName());
    }
}
