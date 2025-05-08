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

import java.util.Map;

import org.eclipse.sisu.Parameters;

import com.google.inject.Key;

/**
 * Useful {@link Key}s for binding {@link Parameters}.
 */
@SuppressWarnings( "rawtypes" )
public interface ParameterKeys
{
    /**
     * <code>{@link Key}.get( Map.class, {@link Parameters}.class );</code>
     */
    Key<Map> PROPERTIES = Key.get( Map.class, Parameters.class );

    /**
     * <code>{@link Key}.get( String[].class, {@link Parameters}.class );</code>
     */
    Key<String[]> ARGUMENTS = Key.get( String[].class, Parameters.class );
}
