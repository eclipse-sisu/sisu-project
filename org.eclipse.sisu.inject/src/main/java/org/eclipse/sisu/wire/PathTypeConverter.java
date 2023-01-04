/*******************************************************************************
 * Copyright (c) 2021-present Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

/**
 * {@link TypeConverter} {@link Module} that converts constants to {@link Path}s.
 */
final class PathTypeConverter
    extends AbstractTypeConverter<Path>
{
    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        return Paths.get( value );
    }
}
