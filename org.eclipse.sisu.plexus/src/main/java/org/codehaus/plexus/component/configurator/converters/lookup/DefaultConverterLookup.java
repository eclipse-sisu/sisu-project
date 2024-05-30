/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
package org.codehaus.plexus.component.configurator.converters.lookup;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.basic.BooleanConverter;
import org.codehaus.plexus.component.configurator.converters.basic.ByteConverter;
import org.codehaus.plexus.component.configurator.converters.basic.CharConverter;
import org.codehaus.plexus.component.configurator.converters.basic.DateConverter;
import org.codehaus.plexus.component.configurator.converters.basic.DoubleConverter;
import org.codehaus.plexus.component.configurator.converters.basic.EnumConverter;
import org.codehaus.plexus.component.configurator.converters.basic.FileConverter;
import org.codehaus.plexus.component.configurator.converters.basic.FloatConverter;
import org.codehaus.plexus.component.configurator.converters.basic.IntConverter;
import org.codehaus.plexus.component.configurator.converters.basic.LongConverter;
import org.codehaus.plexus.component.configurator.converters.basic.PathConverter;
import org.codehaus.plexus.component.configurator.converters.basic.ShortConverter;
import org.codehaus.plexus.component.configurator.converters.basic.StringBufferConverter;
import org.codehaus.plexus.component.configurator.converters.basic.StringBuilderConverter;
import org.codehaus.plexus.component.configurator.converters.basic.StringConverter;
import org.codehaus.plexus.component.configurator.converters.basic.TemporalConverter;
import org.codehaus.plexus.component.configurator.converters.basic.UriConverter;
import org.codehaus.plexus.component.configurator.converters.basic.UrlConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ArrayConverter;
import org.codehaus.plexus.component.configurator.converters.composite.CollectionConverter;
import org.codehaus.plexus.component.configurator.converters.composite.MapConverter;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.composite.PlexusConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.composite.PropertiesConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.eclipse.sisu.inject.Weak;

public final class DefaultConverterLookup
    implements ConverterLookup
{
    private static final ConfigurationConverter[] DEFAULT_CONVERTERS = {
        // optimized ordering...
        new FileConverter(), //
        new BooleanConverter(), //
        new StringConverter(), //
        new IntConverter(), //
        new CollectionConverter(), //
        new ArrayConverter(), //
        new MapConverter(), //
        new PropertiesConverter(), //
        new UrlConverter(), //
        new UriConverter(), //
        new PathConverter(), //
        new DateConverter(), //
        new EnumConverter(), //
        new LongConverter(), //
        new FloatConverter(), //
        new DoubleConverter(), //
        new CharConverter(), //
        new ByteConverter(), //
        new ShortConverter(), //
        // new ClassConverter(), // not installed by default
        new PlexusConfigurationConverter(), //
        new ClassRealmConverter(), //
        new StringBufferConverter(), //
        new StringBuilderConverter(), //
        new TemporalConverter(), //
        new ObjectWithFieldsConverter() };

    private final Map<Class<?>, ConfigurationConverter> lookupCache = //
        Weak.concurrentKeys(); // entries will expire on class unload

    private final List<ConfigurationConverter> customConverters = //
        new CopyOnWriteArrayList<ConfigurationConverter>();

    public void registerConverter( final ConfigurationConverter converter )
    {
        customConverters.add( converter );
    }

    public ConfigurationConverter lookupConverterForType( final Class<?> type )
        throws ComponentConfigurationException
    {
        ConfigurationConverter converter = lookupCache.get( type );
        if ( null != converter )
        {
            return converter;
        }
        for ( int i = 0; i < customConverters.size(); i++ )
        {
            converter = customConverters.get( i );
            if ( converter.canConvert( type ) )
            {
                lookupCache.put( type, converter );
                return converter;
            }
        }
        for ( int i = 0; i < DEFAULT_CONVERTERS.length; i++ )
        {
            converter = DEFAULT_CONVERTERS[i];
            if ( converter.canConvert( type ) )
            {
                lookupCache.put( type, converter );
                return converter;
            }
        }
        throw new ComponentConfigurationException( "Cannot find converter for type: " + type );
    }

    /*
     * Referenced by some external XML configurations
     */
    void setCustomConverters( final List<ConfigurationConverter> converters )
    {
        customConverters.addAll( converters );
    }
}
