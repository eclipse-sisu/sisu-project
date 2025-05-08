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
