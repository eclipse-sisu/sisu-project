/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link Module} that provides JSR250 lifecycle management by following {@link javax.annotation.PostConstruct} and
 * {@link javax.annotation.PreDestroy} annotations. The lifecycle can be controlled with the associated {@link BeanManager}.
 */
public final class LifecycleModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    /* These classes map the Guice SPI to the BeanManager SPI */

    private final Matcher<TypeLiteral<?>> matcher = new AbstractMatcher<TypeLiteral<?>>()
    {
        public boolean matches( final TypeLiteral<?> type )
        {
            return manager.manage( type.getRawType() );
        }
    };

    private final TypeListener typeListener = new TypeListener()
    {
        private final InjectionListener<Object> listener = new InjectionListener<Object>()
        {
            public void afterInjection( final Object bean )
            {
                manager.manage( bean );
            }
        };

        public <B> void hear( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
        {
            encounter.register( listener );
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
        binder.bindListener( matcher, typeListener );
    }
}
