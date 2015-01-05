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
