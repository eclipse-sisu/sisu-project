/*******************************************************************************
 * Copyright (c) 2008, 2014 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/

package org.eclipse.sisu.peaberry.eclipse;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Import;

/**
 * Filtered iterable view over dynamic collection of {@link ExtensionImport}s.
 * <p>
 * The iterator provided by this view is valid even if the underlying collection
 * of extensions changes because it keeps track of where it would be in the list
 * based on an internally-allocated sequence number.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class IterableExtension<T>
    implements Iterable<Import<T>> {

  final ExtensionListener listener;
  final AttributeFilter filter;

  IterableExtension(final ExtensionListener listener, final AttributeFilter filter) {
    this.listener = listener;
    this.filter = filter;
  }

  public Iterator<Import<T>> iterator() {
    return new Iterator<Import<T>>() {

      // keep track of where we've been...
      private ExtensionImport prevImport;
      private ExtensionImport nextImport;

      public boolean hasNext() {
        return null != findNextImport();
      }

      @SuppressWarnings("unchecked")
      public Import<T> next() {

        if (null == findNextImport()) {
          throw new NoSuchElementException();
        }

        // used cached result
        prevImport = nextImport;
        nextImport = null;

        return (Import) prevImport;
      }

      private ExtensionImport findNextImport() {
        if (null == nextImport) {

          // based on our last result and the current list, find next result...
          final ExtensionImport tempImport = listener.findNextImport(prevImport, filter);

          // ...and cache it
          if (null != tempImport) {
            prevImport = null;
            nextImport = tempImport;
          }
        }

        return nextImport;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean equals(final Object rhs) {
    if (rhs instanceof IterableExtension<?>) {
      final IterableExtension<?> iterable = (IterableExtension<?>) rhs;
      return listener.equals(iterable.listener) && equals(filter, iterable.filter);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return listener.hashCode() ^ (null == filter ? 0 : filter.hashCode());
  }

  private static boolean equals(final Object lhs, final Object rhs) {
    return null == lhs ? null == rhs : lhs.equals(rhs);
  }
}
