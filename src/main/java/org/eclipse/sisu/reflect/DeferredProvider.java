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
package org.eclipse.sisu.reflect;

import com.google.inject.Provider;

/**
 * {@link Provider} backed by a {@link DeferredClass}.
 */
public interface DeferredProvider<T>
    extends Provider<T>
{
    /**
     * @return Deferred implementation class
     */
    DeferredClass<T> getImplementationClass();
}
