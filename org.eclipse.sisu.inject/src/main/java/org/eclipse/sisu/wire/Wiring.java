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
