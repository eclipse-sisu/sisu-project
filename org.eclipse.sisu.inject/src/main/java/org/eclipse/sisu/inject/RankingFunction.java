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
package org.eclipse.sisu.inject;

import com.google.inject.Binding;

/**
 * Assigns each {@link Binding} a rank according to some function; higher ranks take precedence over lower ranks.
 */
public interface RankingFunction {
    /**
     * Estimates the maximum rank this function may assign to a {@link Binding}.
     *
     * @return Maximum rank
     * @see BindingPublisher#maxBindingRank()
     */
    int maxRank();

    /**
     * Assigns a numeric rank to the given binding.
     *
     * @param binding The binding
     * @return Assigned rank
     */
    <T> int rank(Binding<T> binding);
}
