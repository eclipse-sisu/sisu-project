/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject;

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
