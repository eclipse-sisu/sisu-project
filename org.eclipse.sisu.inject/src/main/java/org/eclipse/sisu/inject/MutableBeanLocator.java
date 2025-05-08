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
package org.eclipse.sisu.inject;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;

/**
 * Mutable {@link BeanLocator} that finds and tracks bindings across zero or more {@link BindingPublisher}s.
 */
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator
{
    /**
     * Adds the given ranked {@link BindingPublisher} and distributes its {@link Binding}s.
     * 
     * @param publisher The new publisher
     * @return {@code true} if the publisher was added; otherwise {@code false}
     */
    boolean add( BindingPublisher publisher );

    /**
     * Removes the given {@link BindingPublisher} and its {@link Binding}s.
     * 
     * @param publisher The old publisher
     * @return {@code true} if the publisher was removed; otherwise {@code false}
     */
    boolean remove( BindingPublisher publisher );

    /**
     * Snapshot of currently registered {@link BindingPublisher}s.
     * 
     * @return The registered {@link BindingPublisher}s
     */
    Iterable<BindingPublisher> publishers();

    /**
     * Removes all known {@link BindingPublisher}s and their {@link Binding}s.
     */
    void clear();
}
