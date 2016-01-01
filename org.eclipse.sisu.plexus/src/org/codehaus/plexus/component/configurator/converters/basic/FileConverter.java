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
package org.codehaus.plexus.component.configurator.converters.basic;

import java.io.File;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class FileConverter
    extends AbstractBasicConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return File.class.equals( type );
    }

    @Override
    public Object fromString( final String value )
        throws ComponentConfigurationException
    {
        return new File( value.replace( '/' == File.separatorChar ? '\\' : '/', File.separatorChar ) );
    }

    @Override
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final Object result =
            super.fromConfiguration( lookup, configuration, type, enclosingType, loader, evaluator, listener );

        return result instanceof File ? evaluator.alignToBaseDirectory( (File) result ) : result;
    }
}
