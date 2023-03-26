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

import java.util.Iterator;

import org.eclipse.sisu.BeanEntry;

import com.google.inject.name.Named;

/**
 * Sequence of {@link PlexusBean}s backed by {@link BeanEntry}s.
 */
final class DefaultPlexusBeans<T>
    implements Iterable<PlexusBean<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    Iterable<BeanEntry<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DefaultPlexusBeans( final Iterable<BeanEntry<Named, T>> beans )
    {
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<PlexusBean<T>> iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link PlexusBean} iterator backed by {@link BeanEntry}s.
     */
    final class Itr
        implements Iterator<PlexusBean<T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<Named, T>> itr = beans.iterator();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            return itr.hasNext();
        }

        public PlexusBean<T> next()
        {
            return new LazyPlexusBean<T>( itr.next() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
