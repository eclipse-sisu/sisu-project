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
package org.codehaus.plexus.component.configurator.converters.composite;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public final class ObjectWithFieldsConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( final Class<?> type )
    {
        throw new UnsupportedOperationException();
    }

    public void processConfiguration( final ConverterLookup lookup, final Object component, final ClassLoader loader,
                                      final PlexusConfiguration configuration, final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        processConfiguration( lookup, component, loader, configuration, evaluator, null );
    }

    public void processConfiguration( final ConverterLookup lookup, final Object component, final ClassLoader loader,
                                      final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                      final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }
}
