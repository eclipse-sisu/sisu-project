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
package org.eclipse.sisu.locators;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.sisu.BeanEntry;

import com.google.inject.Binding;
import com.google.inject.Key;

/**
 * Provides a sequence of {@link BeanEntry}s by iterating over qualified {@link Binding}s.
 * 
 * @see BeanLocator#locate(Key)
 */
final class LocatedBeans<Q extends Annotation, T>
    implements Iterable<BeanEntry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Key<T> key;

    final RankedBindings<T> explicitBindings;

    final ImplicitBindings implicitBindings;

    final QualifyingStrategy strategy;

    final BeanCache<Q, T> beans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LocatedBeans( final Key<T> key, final RankedBindings<T> explicitBindings, final ImplicitBindings implicitBindings )
    {
        this.key = key;

        this.explicitBindings = explicitBindings;
        this.implicitBindings = implicitBindings;

        strategy = QualifyingStrategy.selectFor( key );
        beans = explicitBindings.newBeanCache();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanEntry<Q, T>> iterator()
    {
        return new Itr();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BeanEntry} iterator that creates new elements from {@link Binding}s as required.
     */
    final class Itr
        implements Iterator<BeanEntry<Q, T>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final RankedBindings<T>.Itr itr = explicitBindings.iterator();

        private final Map<Binding<T>, BeanEntry<Q, T>> readCache = beans.flush();

        private boolean checkImplicitBindings = implicitBindings != null;

        private BeanEntry<Q, T> nextBean;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        public boolean hasNext()
        {
            if ( null != nextBean )
            {
                return true;
            }
            while ( itr.hasNext() )
            {
                final Binding<T> binding = itr.next();
                if ( null != readCache && null != ( nextBean = readCache.get( binding ) ) )
                {
                    return true;
                }
                final Q qualifier = (Q) strategy.qualifies( key, binding );
                if ( null != qualifier )
                {
                    nextBean = beans.create( qualifier, binding, itr.rank() );
                    return true;
                }
            }
            if ( checkImplicitBindings )
            {
                // last-chance, see if we can locate a valid implicit binding somewhere
                final Binding<T> binding = implicitBindings.get( key.getTypeLiteral() );
                if ( null != binding )
                {
                    nextBean = beans.create( (Q) QualifyingStrategy.DEFAULT_QUALIFIER, binding, Integer.MIN_VALUE );
                    return true;
                }
            }
            return false;
        }

        public BeanEntry<Q, T> next()
        {
            if ( hasNext() )
            {
                // no need to check this again
                checkImplicitBindings = false;

                // populated by hasNext()
                final BeanEntry<Q, T> bean = nextBean;
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
