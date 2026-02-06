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
package org.eclipse.sisu.launch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.sisu.space.BeanScanning;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void testBootWithProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("sisu.scanning", "off");
        final Injector injector = Main.boot(properties, new String[0]);
        assertNotNull(injector);
    }

    @Test
    void testBootWithPropertiesAndBindings() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("sisu.scanning", "off");
        final Module extra = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("extra");
            }
        };
        final Injector injector = Main.boot(properties, new String[] {"--test"}, extra);
        assertNotNull(injector);
        assertNotNull(injector.getInstance(String.class));
    }

    @Test
    void testWireModule() {
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("wired");
            }
        };
        final Module wired = Main.wire(BeanScanning.OFF, module);
        assertNotNull(wired);
    }
}
