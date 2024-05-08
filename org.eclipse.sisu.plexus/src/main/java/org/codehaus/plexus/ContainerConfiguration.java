/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;

public interface ContainerConfiguration
{
    ContainerConfiguration setName( String name );

    ContainerConfiguration setContainerConfiguration( String configurationPath );

    String getContainerConfiguration();

    ContainerConfiguration setContainerConfigurationURL( URL configurationUrl );

    URL getContainerConfigurationURL();

    ContainerConfiguration setClassWorld( ClassWorld classWorld );

    ClassWorld getClassWorld();

    ContainerConfiguration setRealm( ClassRealm classRealm );

    ClassRealm getRealm();

    ContainerConfiguration setContext( Map<Object, Object> context );

    Map<Object, Object> getContext();

    ContainerConfiguration setComponentVisibility( String visibility );

    String getComponentVisibility();

    ContainerConfiguration setAutoWiring( boolean on );

    boolean getAutoWiring();

    ContainerConfiguration setClassPathScanning( String scanning );

    String getClassPathScanning();

    ContainerConfiguration setContextComponent( Context context );

    Context getContextComponent();

    ContainerConfiguration setJSR250Lifecycle( boolean on );

    boolean getJSR250Lifecycle();

    ContainerConfiguration setStrictClassPathScanning( boolean strictScanning );

    boolean getStrictClassPathScanning();

}
