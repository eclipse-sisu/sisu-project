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

package org.eclipse.sisu.peaberry;

/**
 * A registry is a {@link ServiceWatcher} that allows lookup of services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface ServiceRegistry
    extends ServiceWatcher<Object> {

  /**
   * Lookup services from the registry, constrained by the given filter.
   * 
   * @param clazz expected service interface
   * @param filter service attribute filter
   * @return ordered sequence of imported services, most recommended first
   */
  <T> Iterable<Import<T>> lookup(Class<T> clazz, AttributeFilter filter);

  /**
   * Watch for services in the registry, constrained by the given filter.
   * 
   * @param clazz expected service interface
   * @param filter service attribute filter
   * @param watcher the watcher that should receive any matching services
   * 
   * @throws UnsupportedOperationException if watching is not supported
   */
  <T> void watch(Class<T> clazz, AttributeFilter filter, ServiceWatcher<? super T> watcher);
}
