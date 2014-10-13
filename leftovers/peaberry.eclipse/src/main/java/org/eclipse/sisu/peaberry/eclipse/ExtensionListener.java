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

import static java.util.Collections.binarySearch;
import static java.util.logging.Level.WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.ServiceWatcher;

/**
 * Keep track of imported Eclipse Extensions that provide a specific interface.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ExtensionListener
    implements IRegistryEventListener {

  private static final Logger LOGGER = Logger.getLogger(ExtensionListener.class.getName());

  private final Class<?> clazz;

  private final String point;
  private final boolean aggregate;
  private long idCounter;

  private final List<ExtensionImport> imports;
  private final List<ServiceWatcher<Object>> watchers;

  ExtensionListener(final Class<?> clazz) {
    final ExtensionBean metadata = clazz.getAnnotation(ExtensionBean.class);

    this.clazz = clazz;

    // no annotation => use lower-case class as point id
    if (null == metadata || metadata.value().length() == 0) {
      point = clazz.getName().toLowerCase() + 's';
    } else {
      point = metadata.value();
    }

    // do we need to combine elements into a single bean?
    aggregate = null != metadata && metadata.aggregate();

    imports = new ArrayList<ExtensionImport>(4);
    watchers = new ArrayList<ServiceWatcher<Object>>(2);
  }

  synchronized void start(final IExtensionRegistry registry) {
    final IExtensionPoint[] extensionPoints;

    // register listener first to avoid race condition
    if (Object.class == clazz || IConfigurationElement.class == clazz) {
      registry.addListener(this);
      extensionPoints = registry.getExtensionPoints();
    } else {
      registry.addListener(this, point);
      extensionPoints = new IExtensionPoint[]{registry.getExtensionPoint(point)};
    }

    // safety check in case there was no matching extension point
    if (extensionPoints.length == 0 || null == extensionPoints[0]) {
      return;
    }

    final Set<IConfigurationElement> ignore = getExistingConfigurationElements();

    // retrieve any matching extensions for each point
    for (final IExtensionPoint p : extensionPoints) {
      for (final IExtension e : p.getExtensions()) {
        insertExtension(e, ignore);
      }
    }
  }

  public synchronized void added(final IExtension[] extensions) {
    final Set<IConfigurationElement> ignore = getExistingConfigurationElements();

    // each extension can have many configs
    for (final IExtension e : extensions) {
      insertExtension(e, ignore);
    }
  }

  private Set<IConfigurationElement> getExistingConfigurationElements() {
    final Set<IConfigurationElement> elements = new HashSet<IConfigurationElement>();
    for (final ExtensionImport i : imports) {
      elements.add(i.getConfigurationElement());
    }
    return elements;
  }

  public synchronized void removed(final IExtension[] extensions) {
    final List<IExtension> candidates = Arrays.asList(extensions);
    for (final Iterator<ExtensionImport> i = imports.iterator(); i.hasNext();) {
      if (i.next().invalidate(candidates)) {
        i.remove();
      }
    }
  }

  public void added(final IExtensionPoint[] points) {/* do nothing */}

  public void removed(final IExtensionPoint[] points) {/* do nothing */}

  @SuppressWarnings("unchecked")
  synchronized void addWatcher(final ServiceWatcher watcher) {
    if (!watchers.contains(watcher) && watchers.add(watcher)) {

      // report existing imports to the new watcher
      for (final ExtensionImport i : imports) {
        notifyWatcher(watcher, i);
      }
    }
  }

  private void insertExtension(final IExtension extension, final Set<IConfigurationElement> ignore) {
    final List<IConfigurationElement> candidates = new ArrayList<IConfigurationElement>();

    if (aggregate) {
      candidates.add(new AggregatedExtension(extension));
    } else {
      Collections.addAll(candidates, extension.getConfigurationElements());
    }

    candidates.removeAll(ignore);

    // create an import for each major configuration element
    for (final IConfigurationElement config : candidates) {
      final ExtensionImport i = new ExtensionImport(++idCounter, config, clazz); // NOPMD
      imports.add(i);

      // report the new import to any watching watchers
      for (final ServiceWatcher<Object> w : watchers) {
        notifyWatcher(w, i);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void notifyWatcher(final ServiceWatcher watcher, final ExtensionImport i) {
    try {
      final Export export = watcher.add(i);
      if (null != export) {
        i.addWatcher(export);
      }
    } catch (final RuntimeException re) {
      LOGGER.log(WARNING, "Exception in service watcher", re);
    }
  }

  synchronized ExtensionImport findNextImport(final ExtensionImport prevImport,
      final AttributeFilter filter) {

    if (imports.isEmpty()) {
      return null;
    }

    if (null == prevImport && null == filter) {
      return imports.get(0);
    }

    // estimate last position based on previous value and current list
    return findNextImport(filter, null == prevImport ? ~0 : binarySearch(imports, prevImport));
  }

  private ExtensionImport findNextImport(final AttributeFilter filter, final int prevIndex) {

    // may need to flip position if previous import is no longer in the list
    for (int i = prevIndex < 0 ? ~prevIndex : prevIndex + 1; i < imports.size(); i++) {

      // now do a linear search applying the given filter
      final ExtensionImport nextImport = imports.get(i);
      if (null == filter || filter.matches(nextImport.attributes())) {
        return nextImport;
      }
    }

    return null; // no matching extension
  }
}
