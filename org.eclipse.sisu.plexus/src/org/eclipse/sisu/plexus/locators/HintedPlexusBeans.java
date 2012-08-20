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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.plexus.config.PlexusBean;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

final class HintedPlexusBeans<T>
    implements Iterable<PlexusBean<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BeanEntry<Named, T>> beans;

    private final List<PlexusBean<T>> missingPlexusBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HintedPlexusBeans( final Iterable<BeanEntry<Named, T>> beans, final TypeLiteral<T> role, final String[] hints )
    {
        this.beans = beans;

        missingPlexusBeans = new ArrayList<PlexusBean<T>>( hints.length );
        for ( final String h : hints )
        {
            missingPlexusBeans.add( new MissingPlexusBean<T>( role, h ) );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<PlexusBean<T>> iterator()
    {
        final List<PlexusBean<T>> plexusBeans = new ArrayList<PlexusBean<T>>( missingPlexusBeans );

        final int size = plexusBeans.size();
        final Iterator<BeanEntry<Named, T>> itr = beans.iterator();
        for ( int numFound = 0; numFound < size && itr.hasNext(); )
        {
            final BeanEntry<Named, T> candidate = itr.next();
            final String hint = candidate.getKey().value();
            for ( int i = 0; i < size; i++ )
            {
                final PlexusBean<T> element = plexusBeans.get( i );
                if ( element instanceof MissingPlexusBean<?> && hint.equals( element.getKey() ) )
                {
                    plexusBeans.set( i, new LazyPlexusBean<T>( candidate ) );
                    numFound++;
                }
            }
        }

        return plexusBeans.iterator();
    }
}
