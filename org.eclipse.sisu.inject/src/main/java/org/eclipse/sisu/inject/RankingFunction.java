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
package org.eclipse.sisu.inject;

import com.google.inject.Binding;

/**
 * Assigns each {@link Binding} a rank according to some function; higher ranks take precedence over lower ranks.
 */
public interface RankingFunction
{
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
    <T> int rank( Binding<T> binding );
}
