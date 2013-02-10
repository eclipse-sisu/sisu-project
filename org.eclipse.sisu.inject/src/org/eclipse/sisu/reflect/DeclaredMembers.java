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
package org.eclipse.sisu.reflect;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} that iterates over declared members of a class hierarchy.
 */
public final class DeclaredMembers
    implements Iterable<Member>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> clazz;

    private final View[] views;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DeclaredMembers( final Class<?> clazz, final View... views )
    {
        this.clazz = clazz;
        this.views = views.length == 0 ? View.values() : views;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<Member> iterator()
    {
        return new MemberIterator( clazz, views );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Read-only {@link Iterator} that uses rolling {@link View}s to traverse the different members.
     */
    private static final class MemberIterator
        implements Iterator<Member>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final Member[] NO_MEMBERS = {};

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Class<?> clazz;

        private final View[] views;

        private int viewIndex;

        private Member[] members = NO_MEMBERS;

        private int memberIndex;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        MemberIterator( final Class<?> clazz, final View[] views )
        {
            this.clazz = clazz;
            this.views = views;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            while ( memberIndex <= 0 )
            {
                if ( viewIndex >= views.length )
                {
                    // reset view
                    clazz = clazz.getSuperclass();
                    viewIndex = 0;
                }

                if ( null == clazz || clazz == java.lang.Object.class )
                {
                    return false;
                }

                final int index = viewIndex++;
                members = views[index].members( clazz );
                memberIndex = members.length;
            }

            return true;
        }

        public Member next()
        {
            if ( hasNext() )
            {
                // initialized by hasNext()
                return members[--memberIndex];
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link Enum} implementation that provides different views of a class's members.
     */
    public static enum View
    {
        CONSTRUCTORS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredConstructors();
            }
        },
        METHODS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredMethods();
            }
        },
        FIELDS
        {
            @Override
            final Member[] members( final Class<?> clazz )
            {
                return clazz.getDeclaredFields();
            }
        };

        abstract Member[] members( final Class<?> clazz );
    }
}
