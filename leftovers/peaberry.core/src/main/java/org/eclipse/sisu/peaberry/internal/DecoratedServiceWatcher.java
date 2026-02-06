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

import java.util.Map;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceWatcher;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;
import org.eclipse.sisu.peaberry.util.DelegatingImport;
import org.eclipse.sisu.peaberry.util.SimpleExport;

/**
 * A {@link ServiceWatcher} decorated by the given {@link ImportDecorator}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class DecoratedServiceWatcher<S>
    implements ServiceWatcher<S> {

  private final ImportDecorator<? super S> decorator;
  private final ServiceWatcher<? super S> watcher;

  DecoratedServiceWatcher(final ImportDecorator<? super S> decorator,
      final ServiceWatcher<? super S> watcher) {
    this.decorator = decorator;
    this.watcher = watcher;
  }

  public <T extends S> Export<T> add(final Import<T> service) {

    // wrap service to allow updates, decorate the wrapper, then publish
    final Export<T> original = new SimpleExport<>(service);
    final Import<T> decorated = decorator.decorate(original);
    final Export<T> published = watcher.add(decorated);

    // watcher is not interested!
    if (null == published) {
      return null;
    }

    return new DecoratedExport<>(original, decorated, published);
  }

  private static final class DecoratedExport<T>
      extends DelegatingImport<T>
      implements Export<T> {

    private final Export<T> original;
    private final Import<T> decorated;
    private final Export<T> published;

    DecoratedExport(final Export<T> original, final Import<T> decorated, final Export<T> published) {
      super(original);

      this.original = original;
      this.decorated = decorated;
      this.published = published;
    }

    public synchronized void put(final T newInstance) {
      // force decoration of new instance
      original.put(newInstance);
      published.put(null == newInstance ? null : decorated.get());
    }

    public synchronized void attributes(final Map<String, ?> attributes) {
      // force decoration of new attributes
      original.attributes(attributes);
      published.attributes(decorated.attributes());
    }

    public void unput() {
      put(null); // simple alias
    }
  }

  @Override
  public boolean equals(final Object rhs) {
    if (rhs instanceof DecoratedServiceWatcher<?>) {
      final DecoratedServiceWatcher<?> decoratedWatcher = (DecoratedServiceWatcher<?>) rhs;
      return decorator.equals(decoratedWatcher.decorator)
          && watcher.equals(decoratedWatcher.watcher);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return decorator.hashCode() ^ watcher.hashCode();
  }
}
