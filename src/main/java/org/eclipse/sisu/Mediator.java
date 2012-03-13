/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Annotation;

/**
 * Mediates bean entries sent from the {@code BeanLocator} to associated watching objects.
 */
public interface Mediator<Q extends Annotation, T, W>
{
    /**
     * Processes the added {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The added bean entry
     * @param watcher The watching object
     */
    void add( BeanEntry<Q, T> entry, W watcher )
        throws Exception;

    /**
     * Processes the removed {@link BeanEntry} and sends the necessary updates to the watcher.
     * 
     * @param entry The removed bean entry
     * @param watcher The watching object
     */
    void remove( BeanEntry<Q, T> entry, W watcher )
        throws Exception;
}
