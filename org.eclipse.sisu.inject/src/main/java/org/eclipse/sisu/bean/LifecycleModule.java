/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
package org.eclipse.sisu.bean;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

/**
 * Guice {@link Module} that provides lifecycle management by following {@link org.eclipse.sisu.PostConstruct}
 * and {@link org.eclipse.sisu.PreDestroy} annotations, or corresponding JSR250 {@link javax.annotation.PostConstruct}
 * and {@link javax.annotation.PreDestroy} annotations. The lifecycle can be controlled with the associated
 * {@link BeanManager}.
 */
public final class LifecycleModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /* These classes map the Guice SPI to the BeanManager SPI */

    private final ProvisionListener provisionListener = new ProvisionListener()
    {
        public <T> void onProvision(final ProvisionInvocation<T> provision)
        {
            manager.manage(provision.provision());
        }
    };

    final BeanManager manager;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public LifecycleModule()
    {
        this( new LifecycleManager() );
    }

    public LifecycleModule( final BeanManager manager )
    {
        this.manager = manager;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.bind( BeanManager.class ).toInstance( manager );
        binder.bindListener( Matchers.any(), provisionListener );
    }
}
