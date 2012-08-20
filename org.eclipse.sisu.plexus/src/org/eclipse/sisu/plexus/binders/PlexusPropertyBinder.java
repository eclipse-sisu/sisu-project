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

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.inject.PropertyBinder;
import org.eclipse.sisu.inject.PropertyBinding;
import org.eclipse.sisu.plexus.config.PlexusBeanMetadata;
import org.eclipse.sisu.reflect.BeanProperty;

import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanPropertyBinder} that auto-binds properties according to Plexus metadata.
 */
final class PlexusPropertyBinder
    implements PropertyBinder
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean optionalSupported = true;
        try
        {
            // support both old and new forms of @Requirement
            Requirement.class.getDeclaredMethod( "optional" );
        }
        catch ( final Exception e )
        {
            optionalSupported = false;
        }
        catch ( final LinkageError e )
        {
            optionalSupported = false;
        }
        OPTIONAL_SUPPORTED = optionalSupported;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean OPTIONAL_SUPPORTED;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanManager manager;

    private final PlexusBeanMetadata metadata;

    private final PlexusConfigurations configurations;

    private final PlexusRequirements requirements;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusPropertyBinder( final PlexusBeanManager manager, final TypeEncounter<?> encounter,
                          final PlexusBeanMetadata metadata )
    {
        this.manager = manager;
        this.metadata = metadata;

        configurations = new PlexusConfigurations( encounter );
        requirements = new PlexusRequirements( encounter );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> PropertyBinding bindProperty( final BeanProperty<T> property )
    {
        if ( metadata.isEmpty() )
        {
            return PropertyBinder.LAST_BINDING;
        }

        /*
         * @Configuration binding
         */
        final Configuration configuration = metadata.getConfiguration( property );
        if ( null != configuration )
        {
            final Provider<T> valueProvider = configurations.lookup( configuration, property );
            return new ProvidedPropertyBinding<T>( property, valueProvider );
        }

        /*
         * @Requirement binding
         */
        final Requirement requirement = metadata.getRequirement( property );
        if ( null != requirement )
        {
            if ( null != manager )
            {
                final PropertyBinding managedBinding = manager.manage( property );
                if ( null != managedBinding )
                {
                    return managedBinding; // the bean manager will handle this property
                }
            }
            final Provider<T> roleProvider = requirements.lookup( requirement, property );
            if ( OPTIONAL_SUPPORTED && requirement.optional() )
            {
                return new OptionalPropertyBinding<T>( property, roleProvider );
            }
            return new ProvidedPropertyBinding<T>( property, roleProvider );
        }

        return null; // nothing to bind
    }
}
