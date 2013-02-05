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
package org.codehaus.plexus.component.configurator.converters;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public abstract class AbstractConfigurationConverter
    implements ConfigurationConverter
{
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> contextType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        return fromConfiguration( lookup, configuration, type, contextType, loader, evaluator, null );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    protected Object fromExpression( final PlexusConfiguration configuration, final ExpressionEvaluator evaluator,
                                     final Class<?> type )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected final String fromXML( final String name )
    {
        throw new UnsupportedOperationException();
    }

    protected final void failIfNotTypeCompatible( final Object value, final Class<?> type,
                                                  final PlexusConfiguration configuration )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    protected final Class<?> getClassForImplementationHint( final Class<?> type,
                                                            final PlexusConfiguration configuration,
                                                            final ClassLoader classLoader )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    protected final Object instantiateObject( final Class<?> type )
        throws ComponentConfigurationException
    {
        throw new UnsupportedOperationException();
    }
}
