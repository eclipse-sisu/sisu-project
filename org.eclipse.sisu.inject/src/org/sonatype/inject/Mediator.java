/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.inject;

import java.lang.annotation.Annotation;

@Deprecated
public interface Mediator<Q extends Annotation, T, W>
{
    void add( BeanEntry<Q, T> entry, W watcher )
        throws Exception;

    void remove( BeanEntry<Q, T> entry, W watcher )
        throws Exception;
}
