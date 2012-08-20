/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

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
}
