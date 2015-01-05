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

import com.google.inject.Key;

/**
 * Something that can supply bindings for unresolved dependency {@link Key}s.
 */
public interface Wiring
{
    /**
     * Attempts to satisfy the given dependency by applying a local binding.
     * 
     * @param key The dependency key
     * @return {@code true} if the wiring succeeded; otherwise {@code false}
     */
    boolean wire( Key<?> key );
}
