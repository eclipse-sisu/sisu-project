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

import static java.util.Collections.binarySearch;
import static java.util.logging.Level.WARNING;
import static org.eclipse.sisu.peaberry.cache.AbstractServiceImport.MODIFIED;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceWatcher;

/**
 * Keep track of imported, sortable services that provide a specific interface.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public abstract class AbstractServiceListener<T> {

  private static final Logger LOGGER = Logger.getLogger(AbstractServiceListener.class.getName());

  private final List<AbstractServiceImport<T>> imports;
  private final List<ServiceWatcher<T>> watchers;

  protected AbstractServiceListener() {
    imports = new ArrayList<>(4);
    watchers = new ArrayList<>(2);
  }

  @SuppressWarnings("unchecked")
  synchronized void addWatcher(final ServiceWatcher watcher) {
    if (!watchers.contains(watcher) && watchers.add(watcher)) {

      // report existing imports to the new watcher
      for (final AbstractServiceImport i : imports) {
        notifyWatcher(watcher, i);
      }
    }
  }

  synchronized void flush(final int targetGeneration) {
    // flush any unused cached service instances
    for (final AbstractServiceImport<T> i : imports) {
      i.flush(targetGeneration);
    }
  }

  protected final void insertService(final AbstractServiceImport<T> i) {
    // find insertion point that maintains ordering
    final int insertIndex = binarySearch(imports, i);

    if (insertIndex < 0) {
      // new object, must flip index
      imports.add(~insertIndex, i);

      // report new import to any watching watchers
      for (final ServiceWatcher<T> w : watchers) {
        notifyWatcher(w, i);
      }
    }
  }

  protected final void updateService(final AbstractServiceImport<T> i) {
    // use linear search in case ranking has changed
    final int index = imports.indexOf(i);

    if (0 <= index) {
      // keep existing instance as it might be in use
      final AbstractServiceImport<T> orig = imports.get(index);

      // need to re-order list?
      if (orig.hasRankingChanged()) {
        imports.remove(index);
        imports.add(~binarySearch(imports, orig), orig);
      }

      // finally update any watchers
      orig.notifyWatchers(MODIFIED);
    } else {
      // not seen before
      insertService(i);
    }
  }

  protected final void removeService(final Import<T> i) {
    // use linear search in case ranking has changed
    final int index = imports.indexOf(i);
    if (0 <= index) {
      // flush cache even if being used
      imports.remove(index).invalidate();
    }
  }

  @SuppressWarnings("unchecked")
  private static void notifyWatcher(final ServiceWatcher watcher, final AbstractServiceImport i) {
    try {
      final Export export = watcher.add(i);
      if (null != export) {
        i.addWatcher(export);
      }
    } catch (final RuntimeException re) {
      LOGGER.log(WARNING, "Exception in service watcher", re);
    }
  }

  synchronized Import<T> findNextImport(final Import<T> prevImport, final AttributeFilter filter) {

    if (imports.isEmpty()) {
      return null;
    }

    if (null == prevImport && null == filter) {
      return imports.get(0);
    }

    // estimate last position based on previous value and current list
    return findNextImport(filter, null == prevImport ? ~0 : binarySearch(imports, prevImport));
  }

  private Import<T> findNextImport(final AttributeFilter filter, final int prevIndex) {

    // may need to flip position if previous import is no longer in the list
    for (int i = prevIndex < 0 ? ~prevIndex : prevIndex + 1; i < imports.size(); i++) {

      // now do a linear search applying the given filter
      final Import<T> nextImport = imports.get(i);
      if (null == filter || filter.matches(nextImport.attributes())) {
        return nextImport;
      }
    }

    return null; // no matching service
  }

  protected abstract void start();
}
