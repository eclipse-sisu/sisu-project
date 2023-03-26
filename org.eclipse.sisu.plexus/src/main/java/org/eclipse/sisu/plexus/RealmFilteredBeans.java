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
import java.util.NoSuchElementException;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.sisu.BeanEntry;

import com.google.inject.name.Named;

/**
 * Sequence of {@link BeanEntry}s filtered according to whether they are visible from the current {@link ClassRealm}.
 */
final class RealmFilteredBeans<T>
    implements Iterable<BeanEntry<Named, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RealmManager realmManager;

    final Iterable<BeanEntry<Named, T>> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RealmFilteredBeans( final RealmManager realmManager, final Iterable<BeanEntry<Named, T>> beans )
    {
        this.realmManager = realmManager;
        this.beans = beans;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanEntry<Named, T>> iterator()
    {
        final Set<String> realmNames = realmManager.visibleRealmNames( RealmManager.contextRealm() );
        if ( null != realmNames && realmNames.size() > 0 )
        {
            return new FilteredItr( realmNames );
        }
        return beans.iterator();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that only returns entries visible from the given set of named realms.
     */
    final class FilteredItr
        implements Iterator<BeanEntry<Named, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<BeanEntry<Named, T>> itr = beans.iterator();

        private final Set<String> realmNames;

        private BeanEntry<Named, T> nextBean;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        public FilteredItr( final Set<String> realmNames )
        {
            this.realmNames = realmNames;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            if ( null != nextBean )
            {
                return true;
            }
            while ( itr.hasNext() )
            {
                nextBean = itr.next();
                final String source = String.valueOf( nextBean.getSource() );
                if ( !source.startsWith( "ClassRealm" ) || realmNames.contains( source ) )
                {
                    return true;
                }
            }
            nextBean = null;
            return false;
        }

        public BeanEntry<Named, T> next()
        {
            if ( hasNext() )
            {
                // populated by hasNext()
                final BeanEntry<Named, T> bean = nextBean;
                nextBean = null;
                return bean;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
