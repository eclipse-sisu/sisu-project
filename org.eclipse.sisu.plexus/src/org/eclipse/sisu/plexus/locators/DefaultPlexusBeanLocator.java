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
package org.eclipse.sisu.plexus.locators;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.sisu.locators.BeanLocator;
import org.eclipse.sisu.plexus.config.PlexusBean;
import org.eclipse.sisu.plexus.config.PlexusBeanLocator;
import org.eclipse.sisu.BeanEntry;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link PlexusBeanLocator} that locates beans of various types from zero or more {@link Injector}s.
 */
@Singleton
public final class DefaultPlexusBeanLocator
    implements PlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String REALM_VISIBILITY = "realm";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanLocator beanLocator;

    private final String visibility;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public DefaultPlexusBeanLocator( final BeanLocator beanLocator )
    {
        this( beanLocator, REALM_VISIBILITY );
    }

    public DefaultPlexusBeanLocator( final BeanLocator beanLocator, final String visibility )
    {
        this.beanLocator = beanLocator;
        this.visibility = visibility;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> Iterable<PlexusBean<T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final Key<T> key = hints.length == 1 ? Key.get( role, Names.named( hints[0] ) ) : Key.get( role, Named.class );
        Iterable<BeanEntry<Named, T>> beans = beanLocator.locate( key );
        if ( REALM_VISIBILITY.equalsIgnoreCase( visibility ) )
        {
            beans = new RealmFilter<T>( beans );
        }
        return hints.length <= 1 ? new DefaultPlexusBeans<T>( beans ) : new HintedPlexusBeans<T>( beans, role, hints );
    }
}
