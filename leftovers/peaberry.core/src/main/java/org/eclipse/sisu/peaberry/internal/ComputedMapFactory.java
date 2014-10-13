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

package org.eclipse.sisu.peaberry.internal;

import static jsr166y.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import jsr166y.ConcurrentReferenceHashMap;
import jsr166y.ConcurrentReferenceHashMap.Option;
import jsr166y.ConcurrentReferenceHashMap.ReferenceType;

/**
 * Provide computed maps based on the JSR166 {@link ConcurrentReferenceHashMap}.
 * 
 * @see http://anonsvn.jboss.org/repos/jbosscache/experimental/jsr166/src/jsr166y
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ComputedMapFactory {

  // instances not allowed
  private ComputedMapFactory() {}

  static final EnumSet<Option> IDENTITY = EnumSet.of(IDENTITY_COMPARISONS);

  /**
   * Computed mapping API.
   */
  interface Function<K, V> {
    V compute(K key);
  }

  /**
   * @return concurrent computed map with the given reference types
   */
  static <K, V> ConcurrentMap<K, V> computedMap(final ReferenceType keyType,
      final ReferenceType valType, final int capacity, final Function<K, V> function) {
    return new ComputedMap<K, V>(keyType, valType, capacity, function);
  }

  private static final class ComputedMap<K, V>
      extends ConcurrentReferenceHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private transient final Function<K, V> function;

    public ComputedMap(final ReferenceType keyType, final ReferenceType valType,
        final int capacity, final Function<K, V> function) {

      // small concurrency level, as most threads just read
      super(capacity, 0.75f, 2, keyType, valType, IDENTITY);
      this.function = function;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
      V value = super.get(key);
      if (null == value) {
        // no mapping, use key to compute a value
        final V newValue = function.compute((K) key);
        value = putIfAbsent((K) key, newValue);
        // /CLOVER:OFF
        if (null == value) {
          // /CLOVER:ON
          return newValue;
        }
      }
      return value;
    }
  }
}
