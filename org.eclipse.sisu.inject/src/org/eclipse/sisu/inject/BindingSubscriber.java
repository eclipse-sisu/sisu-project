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
import com.google.inject.TypeLiteral;

/**
 * Subscriber of {@link Binding}s from one or more {@link BindingPublisher}s.
 */
public interface BindingSubscriber<T>
{
    /**
     * Returns the type of {@link Binding}s that are of interest.
     * 
     * @return The literal type
     */
    TypeLiteral<T> type();

    /**
     * Adds the given ranked {@link Binding} to this subscriber.
     * 
     * @param binding The new binding
     * @param rank The assigned rank
     */
    void add( Binding<T> binding, int rank );

    /**
     * Removes the given {@link Binding} from this subscriber.
     * 
     * @param binding The old binding
     */
    void remove( Binding<T> binding );

    /**
     * Snapshot of currently subscribed {@link Binding}s.
     * 
     * @return The subscribed {@link Binding}s
     */
    Iterable<Binding<T>> bindings();
}
