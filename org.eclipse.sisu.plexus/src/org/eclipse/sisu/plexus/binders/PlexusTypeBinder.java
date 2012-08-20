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
package org.eclipse.sisu.plexus.binders;

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.binders.QualifiedTypeBinder;
import org.eclipse.sisu.locators.BeanDescription;
import org.eclipse.sisu.plexus.config.Roles;
import org.eclipse.sisu.plexus.config.Strategies;
import org.eclipse.sisu.plexus.scanners.PlexusTypeListener;
import org.eclipse.sisu.reflect.DeferredClass;
import org.eclipse.sisu.scanners.QualifiedTypeListener;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;

public final class PlexusTypeBinder
    implements PlexusTypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    private final QualifiedTypeListener qualifiedTypeBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusTypeBinder( final Binder binder )
    {
        this.binder = binder;

        qualifiedTypeBinder = new QualifiedTypeBinder( binder );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        qualifiedTypeBinder.hear( qualifier, qualifiedType, source );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void hear( final Component component, final DeferredClass<?> clazz, final Object source )
    {
        final Key roleKey = Roles.componentKey( component );
        final String strategy = component.instantiationStrategy();
        final Class<?> role = component.role();
        final ScopedBindingBuilder sbb;

        final Binder componentBinder = componentBinder( source, component.description() );

        // special case when role is the implementation
        if ( role.getName().equals( clazz.getName() ) )
        {
            if ( roleKey.getAnnotation() != null )
            {
                sbb = componentBinder.bind( roleKey ).to( role );
            }
            else
            {
                sbb = componentBinder.bind( roleKey );
            }
        }
        else if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            sbb = componentBinder.bind( roleKey ).to( clazz.load() ); // no need to defer
        }
        else
        {
            sbb = componentBinder.bind( roleKey ).toProvider( clazz.asProvider() );
        }

        if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !Strategies.PER_LOOKUP.equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Binder componentBinder( final Object source, final String description )
    {
        if ( null != description && description.length() > 0 )
        {
            return binder.withSource( new PlexusBeanDescription( source, description ) );
        }
        return binder.withSource( source );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class PlexusBeanDescription
        implements BeanDescription
    {
        private final Object source;

        private final String description;

        PlexusBeanDescription( final Object source, final String description )
        {
            this.source = source;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public String toString()
        {
            return source.toString();
        }
    }
}
