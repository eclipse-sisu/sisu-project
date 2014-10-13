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

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceWatcher;
import org.eclipse.sisu.peaberry.util.SimpleExport;

/**
 * Pre-filtered {@link ServiceWatcher} that handles mutable services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class FilteredServiceWatcher<S>
    implements ServiceWatcher<S> {

  final AttributeFilter filter;
  final ServiceWatcher<S> watcher;

  public FilteredServiceWatcher(final AttributeFilter filter, final ServiceWatcher<S> watcher) {
    this.filter = filter;
    this.watcher = watcher;
  }

  public <T extends S> Export<T> add(final Import<T> service) {
    // service metadata can change, so must be able to re-check
    return new FilteredExport<T>(service);
  }

  private final class FilteredExport<T extends S>
      extends SimpleExport<T> {

    private Export<T> realExport;

    FilteredExport(final Import<T> service) {
      super(service);

      checkMatchingService();
    }

    private void checkMatchingService() {
      if (filter.matches(super.attributes())) {
        if (null == realExport) {
          // service metadata now matches
          realExport = watcher.add(this);
        }
      } else if (null != realExport) {
        // metadata doesn't match anymore!
        final Export<T> temp = realExport;
        realExport = null;
        temp.unput();
      }
    }

    // Export aspect is only active when service matches filter

    @Override
    public synchronized void put(final T newInstance) {
      super.put(newInstance);

      if (null != realExport) {
        realExport.put(newInstance);
      }
    }

    @Override
    public synchronized void attributes(final Map<String, ?> newAttributes) {
      super.attributes(newAttributes);

      if (null != realExport) {
        realExport.attributes(newAttributes);
      }

      checkMatchingService(); // re-check filter against latest attributes
    }
  }

  @Override
  public boolean equals(final Object rhs) {
    if (rhs instanceof FilteredServiceWatcher<?>) {
      final FilteredServiceWatcher<?> filteredWatcher = (FilteredServiceWatcher<?>) rhs;
      return filter.equals(filteredWatcher.filter) && watcher.equals(filteredWatcher.watcher);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return filter.hashCode() ^ watcher.hashCode();
  }
}
