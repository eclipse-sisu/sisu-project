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
package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.converters.lookup.DefaultConverterLookup;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@SuppressWarnings("deprecation")
public abstract class AbstractComponentConfigurator implements ComponentConfigurator {
    private static final ExpressionEvaluator DEFAULT_EXPRESSION_EVALUATOR = new DefaultExpressionEvaluator();

    protected ConverterLookup converterLookup = new DefaultConverterLookup();

    @Override
    public void configureComponent(
            final Object component, final PlexusConfiguration configuration, final ClassRealm realm)
            throws ComponentConfigurationException {
        configureComponent(component, configuration, DEFAULT_EXPRESSION_EVALUATOR, realm);
    }

    @Override
    public void configureComponent(
            final Object component,
            final PlexusConfiguration configuration,
            final ExpressionEvaluator evaluator,
            final ClassRealm realm)
            throws ComponentConfigurationException {
        configureComponent(component, configuration, evaluator, realm, null);
    }
}
