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
package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public interface ComponentConfigurator
{
    String ROLE = ComponentConfigurator.class.getName();

    void configureComponent( Object component, PlexusConfiguration configuration, ClassRealm realm )
        throws ComponentConfigurationException;

    void configureComponent( Object component, PlexusConfiguration configuration, ExpressionEvaluator evaluator,
                             ClassRealm realm )
        throws ComponentConfigurationException;

    void configureComponent( Object component, PlexusConfiguration configuration, ExpressionEvaluator evaluator,
                             ClassRealm realm, ConfigurationListener listener )
        throws ComponentConfigurationException;
}
