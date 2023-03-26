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
package org.eclipse.sisu.plexus;

import javax.inject.Inject;

import org.codehaus.plexus.PlexusConstants;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.BeanLocator;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
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
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanLocator beanLocator;

    private final RealmManager realmManager;

    private final String visibility;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public DefaultPlexusBeanLocator( final BeanLocator beanLocator )
    {
        this( beanLocator, null, null );
    }

    public DefaultPlexusBeanLocator( final BeanLocator beanLocator, final RealmManager realmManager,
                                     final String visibility )
    {
        this.beanLocator = beanLocator;
        this.realmManager = realmManager;
        this.visibility = visibility;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public <T> Iterable<PlexusBean<T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final Key<T> key = hints.length == 1 ? Key.get( role, Names.named( hints[0] ) ) : Key.get( role, Named.class );
        Iterable<BeanEntry<Named, T>> beans = (Iterable<BeanEntry<Named, T>>) beanLocator.<Named, T> locate( key );
        if ( PlexusConstants.REALM_VISIBILITY.equalsIgnoreCase( visibility ) )
        {
            beans = new RealmFilteredBeans<T>( realmManager, beans );
        }
        return hints.length <= 1 ? new DefaultPlexusBeans<T>( beans ) : new HintedPlexusBeans<T>( beans, role, hints );
    }
}
