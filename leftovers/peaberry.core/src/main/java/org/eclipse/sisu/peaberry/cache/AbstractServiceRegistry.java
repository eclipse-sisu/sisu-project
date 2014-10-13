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

import static org.eclipse.sisu.peaberry.util.Filters.ldap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.ServiceWatcher;

/**
 * Partial {@link ServiceRegistry} implementation with generation based caching.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public abstract class AbstractServiceRegistry
    implements CachingServiceRegistry {

  private final boolean useNativeFilter;

  // per-class/filter map of service listeners (much faster than polling)
  private final ConcurrentMap<String, AbstractServiceListener<?>> listenerMap =
      new ConcurrentHashMap<String, AbstractServiceListener<?>>(16, 0.75f, 2);

  protected AbstractServiceRegistry(final boolean useNativeFilter) {
    this.useNativeFilter = useNativeFilter;
  }

  @SuppressWarnings("unchecked")
  public final <T> Iterable<Import<T>> lookup(final Class<T> clazz, final AttributeFilter filter) {

    // might combine class filter and user filter as one LDAP string
    final AttributeFilter[] filterRef = new AttributeFilter[]{filter};
    final String ldapFilter = getLdapFilter(clazz, filterRef);

    return new FilteredIterableService(registerListener(ldapFilter), filterRef[0]);
  }

  @SuppressWarnings("unchecked")
  public final <T> void watch(final Class<T> clazz, final AttributeFilter filter,
      final ServiceWatcher<? super T> watcher) {

    // might combine class filter and user filter as one LDAP string
    final AttributeFilter[] filterRef = new AttributeFilter[]{filter};
    final String ldapFilter = getLdapFilter(clazz, filterRef);

    if (null == filterRef[0]) {
      registerListener(ldapFilter).addWatcher(watcher);
    } else {
      registerListener(ldapFilter).addWatcher(new FilteredServiceWatcher(filter, watcher));
    }
  }

  public final void flush(final int targetGeneration) {
    // look for unused cached service instances to flush...
    for (final AbstractServiceListener<?> i : listenerMap.values()) {
      i.flush(targetGeneration);
    }
  }

  private AbstractServiceListener<?> registerListener(final String ldapFilter) {
    final String key = null == ldapFilter ? "" : ldapFilter;
    AbstractServiceListener<?> listener;

    listener = listenerMap.get(key);
    if (null == listener) {
      final AbstractServiceListener<?> newListener = createListener(ldapFilter);
      listener = listenerMap.putIfAbsent(key, newListener);
      if (null == listener) {
        newListener.start();
        return newListener;
      }
    }

    return listener;
  }

  private String getLdapFilter(final Class<?> clazz, final AttributeFilter[] filterRef) {
    final String clazzFilter;

    if (null != clazz && Object.class != clazz) { // NOPMD
      clazzFilter = "(objectClass=" + clazz.getName() + ')';
    } else {
      clazzFilter = null;
    }

    if (useNativeFilter && null != filterRef[0]) {
      try {
        // can the user filter object be normalized to an LDAP string?
        final String ldapFilter = ldap(filterRef[0].toString()).toString();
        filterRef[0] = null; // yes, so we don't need object anymore

        return null == clazzFilter ? ldapFilter : "(&" + clazzFilter + ldapFilter + ')';
      } catch (final IllegalArgumentException e) {/* not native LDAP */} // NOPMD
    }

    return clazzFilter;
  }

  protected abstract <T> AbstractServiceListener<T> createListener(String ldapFilter);
}
