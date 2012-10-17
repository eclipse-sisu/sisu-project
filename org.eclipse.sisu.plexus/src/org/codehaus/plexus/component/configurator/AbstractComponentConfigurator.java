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
package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public abstract class AbstractComponentConfigurator
    implements ComponentConfigurator
{
    private static final ExpressionEvaluator DEFAULT_EXPRESSION_EVALUATOR = new DefaultExpressionEvaluator();

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ClassRealm realm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, DEFAULT_EXPRESSION_EVALUATOR, realm );
    }

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, evaluator, realm, null );
    }

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm,
                                    final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }
}
