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

import java.util.Arrays;
import java.util.Iterator;

import javax.inject.Inject;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.ServiceWatcher;

/**
 * A {@link ServiceRegistry} that delegates to a series of other registries.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public class RegistryChain
    implements ServiceRegistry {

  private final ServiceRegistry[] registries;

  @Inject
  public RegistryChain(@Chain final ServiceRegistry mainRegistry,
      @Chain final ServiceRegistry[] extraRegistries) {

    // merge main and additional registries into a single array
    registries = new ServiceRegistry[1 + extraRegistries.length];
    System.arraycopy(extraRegistries, 0, registries, 1, extraRegistries.length);
    registries[0] = mainRegistry;
  }

  public final <T> Iterable<Import<T>> lookup(final Class<T> clazz, final AttributeFilter filter) {

    @SuppressWarnings("unchecked")
    final Iterable<Import<T>>[] lazyIterables = new Iterable[registries.length];

    // support lazy lookup from multiple registries
    for (int i = 0; i < registries.length; i++) {
      final ServiceRegistry reg = registries[i];
      lazyIterables[i] = new Iterable<>() { // NOSONAR
            private volatile Iterable<Import<T>> iterable;

            // delay lookup until absolutely necessary
            public Iterator<Import<T>> iterator() {
              if (null == iterable) {
                synchronized (this) {
                  if (null == iterable) {
                    iterable = reg.lookup(clazz, filter);
                  }
                }
              }
              return iterable.iterator();
            }
          };
    }

    // chain the iterators together
    return new Iterable<Import<T>>() {
      public Iterator<Import<T>> iterator() {
        return new IteratorChain<>(lazyIterables);
      }
    };
  }

  public final <T> void watch(final Class<T> clazz, final AttributeFilter filter,
      final ServiceWatcher<? super T> watcher) {

    for (final ServiceRegistry r : registries) {
      try {
        r.watch(clazz, filter, watcher); // attempt to watch all of them
      } catch (final UnsupportedOperationException e) {/* unable to watch */} // NOSONAR
    }
  }

  public final <T> Export<T> add(final Import<T> service) {
    return registries[0].add(service); // only add to main repository
  }

  @Override
  public final String toString() {
    return String.format("RegistryChain%s", Arrays.toString(registries));
  }

  @Override
  public final int hashCode() {
    return Arrays.hashCode(registries);
  }

  @Override
  public final boolean equals(final Object rhs) {
    if (rhs instanceof RegistryChain) {
      return Arrays.equals(registries, ((RegistryChain) rhs).registries);
    }
    return false;
  }
}
