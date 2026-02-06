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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;

public final class DefaultContainerConfiguration implements ContainerConfiguration {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String configurationPath;

    private URL configurationUrl;

    private ClassWorld classWorld;

    private ClassRealm classRealm;

    private Map<Object, Object> contextData;

    private String componentVisibility = PlexusConstants.REALM_VISIBILITY;

    private String classPathScanning = PlexusConstants.SCANNING_OFF;

    private boolean autoWiring;

    private Context contextComponent;

    private boolean jsr250Lifecycle;

    private boolean strictClassPathScanning;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public ContainerConfiguration setName(final String name) {
        return this;
    }

    @Override
    public ContainerConfiguration setContainerConfiguration(final String configurationPath) {
        this.configurationPath = configurationPath;
        return this;
    }

    @Override
    public String getContainerConfiguration() {
        return configurationPath;
    }

    @Override
    public ContainerConfiguration setContainerConfigurationURL(final URL configurationUrl) {
        this.configurationUrl = configurationUrl;
        return this;
    }

    @Override
    public URL getContainerConfigurationURL() {
        return configurationUrl;
    }

    @Override
    public ContainerConfiguration setClassWorld(final ClassWorld classWorld) {
        this.classWorld = classWorld;
        return this;
    }

    @Override
    public ClassWorld getClassWorld() {
        return classWorld;
    }

    @Override
    public ContainerConfiguration setRealm(final ClassRealm classRealm) {
        this.classRealm = classRealm;
        return this;
    }

    @Override
    public ClassRealm getRealm() {
        return classRealm;
    }

    @Override
    public ContainerConfiguration setContext(final Map<Object, Object> contextData) {
        this.contextData = contextData;
        return this;
    }

    @Override
    public Map<Object, Object> getContext() {
        return contextData;
    }

    @Override
    public ContainerConfiguration setComponentVisibility(final String componentVisibility) {
        this.componentVisibility = componentVisibility;
        return this;
    }

    @Override
    public String getComponentVisibility() {
        return componentVisibility;
    }

    @Override
    public ContainerConfiguration setClassPathScanning(final String classPathScanning) {
        this.classPathScanning = classPathScanning;
        if (!PlexusConstants.SCANNING_OFF.equalsIgnoreCase(classPathScanning)) {
            autoWiring = true;
        }
        return this;
    }

    @Override
    public String getClassPathScanning() {
        return classPathScanning;
    }

    @Override
    public ContainerConfiguration setAutoWiring(final boolean autoWiring) {
        this.autoWiring = autoWiring;
        return this;
    }

    @Override
    public boolean getAutoWiring() {
        return autoWiring;
    }

    @Override
    public ContainerConfiguration setContextComponent(final Context contextComponent) {
        this.contextComponent = contextComponent;
        return this;
    }

    @Override
    public Context getContextComponent() {
        return contextComponent;
    }

    @Override
    public ContainerConfiguration setJSR250Lifecycle(final boolean jsr250Lifecycle) {
        this.jsr250Lifecycle = jsr250Lifecycle;
        return this;
    }

    @Override
    public boolean getJSR250Lifecycle() {
        return jsr250Lifecycle;
    }

    @Override
    public ContainerConfiguration setStrictClassPathScanning(boolean strictScanning) {
        this.strictClassPathScanning = strictScanning;
        return this;
    }

    @Override
    public boolean getStrictClassPathScanning() {
        return strictClassPathScanning;
    }
}
