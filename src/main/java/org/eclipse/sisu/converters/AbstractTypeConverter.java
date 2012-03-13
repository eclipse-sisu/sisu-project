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
package org.eclipse.sisu.converters;

import org.eclipse.sisu.reflect.TypeParameters;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;

/**
 * Abstract {@link TypeConverter} {@link Module} that automatically registers the converter based on the type parameter.
 */
public abstract class AbstractTypeConverter<T>
    implements TypeConverter, Module
{
    public final void configure( final Binder binder )
    {
        // make sure we pick up the right super type parameter, i.e. Foo from AbstractTypeConverter<Foo>
        final TypeLiteral<?> superType = TypeLiteral.get( getClass() ).getSupertype( AbstractTypeConverter.class );
        binder.convertToTypes( Matchers.only( TypeParameters.get( superType, 0 ) ), this );
    }
}
