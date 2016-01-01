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
package org.codehaus.plexus.component.configurator.converters;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public interface ConfigurationConverter
{
    boolean canConvert( Class<?> type );

    Object fromConfiguration( ConverterLookup lookup, PlexusConfiguration configuration, Class<?> type,
                              Class<?> enclosingType, ClassLoader loader, ExpressionEvaluator evaluator )
        throws ComponentConfigurationException;

    Object fromConfiguration( ConverterLookup lookup, PlexusConfiguration configuration, Class<?> type,
                              Class<?> enclosingType, ClassLoader loader, ExpressionEvaluator evaluator,
                              ConfigurationListener listener )
        throws ComponentConfigurationException;
}
