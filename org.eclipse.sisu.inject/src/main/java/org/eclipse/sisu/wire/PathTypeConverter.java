/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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

import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;
import java.io.File;
import java.nio.file.Path;

/**
 * {@link TypeConverter} {@link Module} that converts constants to {@link Path}s.
 */
final class PathTypeConverter extends AbstractTypeConverter<Path> {
    @Override
    public Object convert(final String value, final TypeLiteral<?> toType) {
        return new File(value).toPath();
    }
}
