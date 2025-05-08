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

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;

import com.google.inject.ImplementedBy;
import com.google.inject.Key;

/**
 * Finds and tracks bean implementations annotated with {@link Qualifier} annotations.
 */
@ImplementedBy( MutableBeanLocator.class )
public interface BeanLocator
{
    /**
     * Finds bean implementations that match the given qualified binding {@link Key}.
     * 
     * @param key The qualified key
     * @return Sequence of bean entries that match the given key
     */
    <Q extends Annotation, T> Iterable<? extends BeanEntry<Q, T>> locate( Key<T> key );

    /**
     * Tracks bean implementations that match the given qualified binding {@link Key}.
     * <p>
     * Uses the {@link Mediator} pattern to send events to an arbitrary watcher object.
     * 
     * @param key The qualified key
     * @param mediator The event mediator
     * @param watcher The bean watcher
     */
    <Q extends Annotation, T, W> void watch( Key<T> key, Mediator<Q, T, W> mediator, W watcher );
}
