/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
package org.eclipse.sisu.wire;

import org.eclipse.sisu.inject.TypeArguments;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;

/**
 * Abstract {@link TypeConverter} {@link Module} that automatically registers the converter based on the type argument.
 */
public abstract class AbstractTypeConverter<T>
    implements TypeConverter, Module
{
    public final void configure( final Binder binder )
    {
        // make sure we pick up the right super type argument, i.e. Foo from AbstractTypeConverter<Foo>
        final TypeLiteral<?> superType = TypeLiteral.get( getClass() ).getSupertype( AbstractTypeConverter.class );
        binder.convertToTypes( Matchers.only( TypeArguments.get( superType, 0 ) ), this );
    }
}
