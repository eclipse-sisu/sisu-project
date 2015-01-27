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
package org.eclipse.sisu.inject;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

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

    /**
     * Adds the given ranked {@link Injector} and distributes its {@link Binding}s. Marked as deprecated because most
     * clients should <b>not</b> call this method; any injector with an instance binding to a {@link BeanLocator} is
     * automatically added to that locator as part of the bootstrapping process.
     * 
     * @param injector The new injector
     * @param rank The assigned rank; should reflect the injector's {@link RankingFunction#maxRank()}
     * @deprecated injectors are normally added automatically, clients should not need to call this method
     */
    @Deprecated
    void add( Injector injector, int rank );

    /**
     * Removes the given {@link Injector} and its {@link Binding}s.
     * 
     * @param injector The old injector
     */
    @Deprecated
    void remove( Injector injector );
}
