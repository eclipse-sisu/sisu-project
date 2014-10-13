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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.ServiceWatcher;
import org.eclipse.sisu.peaberry.cache.FilteredServiceWatcher;

/**
 * Eclipse Extension {@link ServiceRegistry} implementation.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class EclipseRegistry
    implements ServiceRegistry {

  private static class SingletonHolder {
    static final ServiceRegistry thisRegistry = new EclipseRegistry();
  }

  /**
   * Create a singleton {@link ServiceRegistry} that queries the default
   * {@link IExtensionRegistry} for named extension point contributions.
   * 
   * @return Eclipse Extension service registry
   */
  public static ServiceRegistry eclipseRegistry() {
    return SingletonHolder.thisRegistry;
  }

  private final IExtensionRegistry extensionRegistry;

  // per-class map of extension listeners (much faster than polling)
  private final ConcurrentMap<String, ExtensionListener> listenerMap =
      new ConcurrentHashMap<String, ExtensionListener>(16, 0.75f, 2);

  EclipseRegistry() {
    extensionRegistry = RegistryFactory.getRegistry();
  }

  public <T> Iterable<Import<T>> lookup(final Class<T> clazz, final AttributeFilter filter) {
    return new IterableExtension<T>(registerListener(clazz), filter);
  }

  public <T> Export<T> add(final Import<T> service) {
    // Extension registry is currently read-only
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public <T> void watch(final Class<T> clazz, final AttributeFilter filter,
      final ServiceWatcher<? super T> watcher) {

    registerListener(clazz).addWatcher(
        null == filter ? watcher : new FilteredServiceWatcher(filter, watcher));
  }

  // see bundle activator
  public void shutdown() {
    for (final String k : listenerMap.keySet()) {
      extensionRegistry.removeListener(listenerMap.remove(k));
    }
  }

  @Override
  public String toString() {
    return String.format("EclipseRegistry[%s]", extensionRegistry.toString());
  }

  @Override
  public int hashCode() {
    return extensionRegistry.hashCode();
  }

  @Override
  public boolean equals(final Object rhs) {
    if (rhs instanceof EclipseRegistry) {
      return extensionRegistry.equals(((EclipseRegistry) rhs).extensionRegistry);
    }
    return false;
  }

  private <T> ExtensionListener registerListener(final Class<T> clazz) {
    final Class<?> safeClazz = null == clazz ? Object.class : clazz;
    final String clazzName = safeClazz.getName();
    ExtensionListener listener;

    listener = listenerMap.get(clazzName);
    if (null == listener) {
      final ExtensionListener newListener = new ExtensionListener(safeClazz);
      listener = listenerMap.putIfAbsent(clazzName, newListener);
      if (null == listener) {
        newListener.start(extensionRegistry);
        return newListener;
      }
    }

    return listener;
  }
}
