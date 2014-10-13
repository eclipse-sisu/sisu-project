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

package org.eclipse.sisu.peaberry.cache;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that iterates over a series of iterators in turn.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class IteratorChain<T>
    implements Iterator<T> {

  private final Iterable<T>[] lazyIterables;
  private final Iterator<T>[] iterators;

  @SuppressWarnings("unchecked")
  IteratorChain(final Iterable<T>[] lazyIterables) {
    this.lazyIterables = lazyIterables.clone();
    iterators = new Iterator[lazyIterables.length];
  }

  private int index;

  public boolean hasNext() {
    // peek ahead, but don't disturb current position
    for (int i = index; i < iterators.length; i++) {
      unroll(i);
      if (iterators[i].hasNext()) {
        return true;
      }
    }
    return false;
  }

  public T next() {
    // move forwards along the chain
    while (index < iterators.length) {
      unroll(index);
      try {
        // is this section finished yet?
        return iterators[index].next();
      } catch (final NoSuchElementException e) {
        index++; // move onto next section
      }
    }
    throw new NoSuchElementException();
  }

  private void unroll(final int i) {
    if (null == iterators[i]) {
      iterators[i] = lazyIterables[i].iterator();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
