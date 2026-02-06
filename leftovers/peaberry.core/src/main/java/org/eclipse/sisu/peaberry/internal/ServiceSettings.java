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

import static org.eclipse.sisu.peaberry.internal.Setting.newSetting;
import static org.eclipse.sisu.peaberry.internal.Setting.nullSetting;

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.ServiceWatcher;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;
import org.eclipse.sisu.peaberry.util.Filters;
import org.eclipse.sisu.peaberry.util.StaticImport;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Maintain state of {@link ServiceBuilderImpl} while the fluent API is used.
 * Also includes a few helpers to simplify the import and export of services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ServiceSettings<T>
    implements Cloneable {

  // initial constant settings
  private final Setting<T> service;
  private final Class<T> clazz;

  // current builder state...
  private Setting<ServiceRegistry> registry = newSetting(Key.get(ServiceRegistry.class));
  private Setting<ImportDecorator<? super T>> decorator = nullSetting();
  private Setting<ServiceWatcher<? super T>> watcher = nullSetting();
  private Setting<Map<String, ?>> attributes = nullSetting();
  private Setting<AttributeFilter> filter = nullSetting();

  /**
   * Configure service based on binding key.
   */
  @SuppressWarnings("unchecked")
  ServiceSettings(final Key<? extends T> key) {
    service = newSetting(key);
    clazz = (Class) key.getTypeLiteral().getRawType();
  }

  /**
   * Configure service based on explicit instance.
   */
  @SuppressWarnings("unchecked")
  ServiceSettings(final T instance) {
    if (null == instance) {
      service = nullSetting();
      clazz = (Class) Object.class;
    } else {
      service = newSetting(instance);
      clazz = (Class) instance.getClass();
    }
  }

  // setters...

  void setDecorator(final Setting<ImportDecorator<? super T>> decorator) {
    this.decorator = decorator;
  }

  void setAttributes(final Setting<Map<String, ?>> attributes) {
    this.attributes = attributes;
  }

  void setFilter(final Setting<AttributeFilter> filter) {
    this.filter = filter;
  }

  void setWatcher(final Setting<ServiceWatcher<? super T>> watcher) {
    this.watcher = watcher;
  }

  void setRegistry(final Setting<ServiceRegistry> registry) {
    this.registry = registry;
  }

  // helper methods...

  @Override
  @SuppressWarnings("unchecked")
  public ServiceSettings<T> clone() {
    try {
      // clone all settings to preserve state
      return (ServiceSettings<T>) super.clone();
    } catch (final CloneNotSupportedException e) {
      // /CLOVER:OFF
      return this;
      // /CLOVER:ON
    }
  }

  // query methods...

  Class<T> getClazz() {
    return clazz;
  }

  ImportDecorator<? super T> getDecorator(final Injector injector) {
    return decorator.get(injector);
  }

  private AttributeFilter getFilter(final Injector injector) {
    final AttributeFilter attributeFilter = filter.get(injector);
    if (null == attributeFilter) {
      // no filter, try using the current attributes as a sample filter
      final Map<String, ?> sampleAttributes = attributes.get(injector);
      if (null != sampleAttributes && !sampleAttributes.isEmpty()) {
        filter = newSetting(Filters.attributes(sampleAttributes));
        return filter.get(injector);
      }
    }
    return attributeFilter;
  }

  Iterable<Import<T>> getImports(final Injector injector, final boolean isConcurrent) {
    final ServiceRegistry serviceRegistry = registry.get(injector);
    final AttributeFilter attributeFilter = getFilter(injector);

    final Iterable<Import<T>> imports = serviceRegistry.lookup(clazz, attributeFilter);

    // enable outjection, but only if it's going to a different watcher
    ServiceWatcher<? super T> serviceWatcher = watcher.get(injector);
    if (null != serviceWatcher && serviceRegistry != serviceWatcher) { // NOSONAR

      final ImportDecorator<? super T> watcherDecorator = decorator.get(injector);
      if (null != watcherDecorator) {
        // decorate the watcher if necessary, to support decorated watching
        serviceWatcher = new DecoratedServiceWatcher<T>(watcherDecorator, serviceWatcher);
      }

      if (isConcurrent) {
        // now apply concurrent behaviour when watching single services
        serviceWatcher = new ConcurrentServiceWatcher<T>(imports, serviceWatcher);
      }

      serviceRegistry.watch(clazz, attributeFilter, serviceWatcher);
    }

    return imports;
  }

  private Export<T> export; // workaround issue 35: Guice calls twice by mistake

  Export<T> getExport(final Injector injector) {

    // this might cause a reentrant call...
    final T instance = service.get(injector);
    if (null == export) {

      // watcher might be null, but registry setting will be non-null
      ServiceWatcher<? super T> serviceWatcher = watcher.get(injector);
      if (null == serviceWatcher) {
        serviceWatcher = registry.get(injector);
      }

      // decorate the watcher if necessary, to support decorated exports
      final ImportDecorator<? super T> watcherDecorator = decorator.get(injector);
      if (null != watcherDecorator) {
        serviceWatcher = new DecoratedServiceWatcher<T>(watcherDecorator, serviceWatcher);
      }

      export = serviceWatcher.add(new StaticImport<T>(instance, attributes.get(injector)));
    }

    return export;
  }
}
