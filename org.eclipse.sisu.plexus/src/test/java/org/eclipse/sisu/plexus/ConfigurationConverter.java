/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

public class ConfigurationConverter
    extends AbstractMatcher<TypeLiteral<?>>
    implements TypeConverter, Module
{
    private Collection<TypeConverterBinding> otherConverterBindings;

    @Inject
    private PlexusBeanConverter beanConverter;

    private boolean available = true;

    public void configure( final Binder binder )
    {
        binder.convertToTypes( this, this );
        binder.requestInjection( this );
    }

    public boolean matches( final TypeLiteral<?> type )
    {
        for ( final TypeConverterBinding b : otherConverterBindings )
        {
            if ( b.getTypeMatcher().matches( type ) )
            {
                return false;
            }
        }
        return available;
    }

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        available = false;
        try
        {
            return beanConverter.convert( toType, value );
        }
        finally
        {
            available = true;
        }
    }

    @Inject
    void setTypeConverterBindings( final Injector injector )
    {
        otherConverterBindings = new ArrayList<TypeConverterBinding>();
        for ( final TypeConverterBinding b : injector.getTypeConverterBindings() )
        {
            if ( false == b.getTypeConverter() instanceof ConfigurationConverter )
            {
                otherConverterBindings.add( b );
            }
        }
    }
}
