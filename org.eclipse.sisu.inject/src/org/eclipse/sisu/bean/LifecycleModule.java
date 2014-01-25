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
package org.eclipse.sisu.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public final class LifecycleModule
    extends AbstractLifecycleManager
    implements TypeListener, InjectionListener<Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final LifecycleMatcher lifecycleMatcher = new LifecycleMatcher();

    private final List<Object> disposableBeans = new ArrayList<Object>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public LifecycleModule( final boolean detectLoops )
    {
        super( detectLoops );
    }

    public LifecycleModule()
    {
        this( true );
    }

    // ----------------------------------------------------------------------
    // Lifecycle methods
    // ----------------------------------------------------------------------

    public boolean manage( final Class<?> clazz )
    {
        return lifecycleMatcher.matches( clazz );
    }

    public PropertyBinding manage( final BeanProperty<?> property )
    {
        return null;
    }

    public boolean manage( final Object bean )
    {
        schedule( bean );
        return true;
    }

    public boolean unmanage( final Object bean )
    {
        return true;
    }

    public boolean unmanage()
    {
        return true;
    }

    // ----------------------------------------------------------------------
    // Configuration methods
    // ----------------------------------------------------------------------

    @Override
    public void configure( final Binder binder )
    {
        super.configure( binder );

        binder.bindListener( lifecycleMatcher, this );
    }

    public <B> void hear( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        encounter.register( this );
    }

    public void afterInjection( final Object bean )
    {
        schedule( bean );
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void activate( final Object bean )
    {
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

}
