/*******************************************************************************
 * Copyright (c) 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.codehaus.plexus.component.configurator.converters.composite;

import java.lang.reflect.Type;

import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

final class CollectionHelper
{
    private final ConverterLookup lookup;

    private final ClassLoader loader;

    private final ExpressionEvaluator evaluator;

    private final ConfigurationListener listener;

    CollectionHelper( final ConverterLookup lookup, final ClassLoader loader, final ExpressionEvaluator evaluator,
                      final ConfigurationListener listener )
    {
        this.lookup = lookup;
        this.loader = loader;
        this.evaluator = evaluator;
        this.listener = listener;
    }

    void addAll( final Object collection, final Type[] parameterTypes, final Class<?> enclosingType,
                 final PlexusConfiguration configuration )
    {
        throw new UnsupportedOperationException();
    }
}
