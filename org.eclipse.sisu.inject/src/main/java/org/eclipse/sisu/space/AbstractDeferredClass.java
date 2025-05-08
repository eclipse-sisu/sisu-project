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
package org.eclipse.sisu.space;

import javax.inject.Inject;

import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.inject.DeferredProvider;
import org.eclipse.sisu.inject.Logs;

import com.google.inject.Injector;

/**
 * Abstract combination of {@link DeferredClass} and {@link DeferredProvider}.
 */
abstract class AbstractDeferredClass<T>
    implements DeferredClass<T>, DeferredProvider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private Injector injector;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final DeferredProvider<T> asProvider()
    {
        return this;
    }

    public final T get()
    {
        try
        {
            // load class and bootstrap injection
            return injector.getInstance( load() );
        }
        catch ( final Throwable e ) // NOPMD see Logs.catchThrowable
        {
            Logs.catchThrowable( e );
            try
            {
                Logs.warn( "Error injecting: {}", getName(), e );
            }
            finally
            {
                Logs.throwUnchecked( e );
            }
        }
        return null; // not used
    }

    public final DeferredClass<T> getImplementationClass()
    {
        return this;
    }
}
