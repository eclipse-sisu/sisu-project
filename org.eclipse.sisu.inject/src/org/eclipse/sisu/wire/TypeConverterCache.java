/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

/**
 * Lazy cache of known {@link TypeConverter}s.
 */
@Singleton
final class TypeConverterCache
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<TypeLiteral<?>, TypeConverter> converterMap =
        new ConcurrentHashMap<TypeLiteral<?>, TypeConverter>( 16, 0.75f, 1 );

    private final Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    TypeConverterCache( final Injector injector )
    {
        this.injector = injector;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public TypeConverter getTypeConverter( final TypeLiteral<?> type )
    {
        TypeConverter converter = converterMap.get( type );
        if ( null == converter )
        {
            for ( final TypeConverterBinding b : injector.getTypeConverterBindings() )
            {
                if ( b.getTypeMatcher().matches( type ) )
                {
                    converter = b.getTypeConverter();
                    converterMap.put( type, converter );
                    break;
                }
            }
        }
        return converter;
    }
}
