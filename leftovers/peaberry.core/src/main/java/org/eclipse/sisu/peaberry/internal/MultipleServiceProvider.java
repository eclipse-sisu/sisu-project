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

import static org.eclipse.sisu.peaberry.internal.DirectServiceFactory.directServices;
import static org.eclipse.sisu.peaberry.internal.ServiceProxyFactory.serviceProxies;

import javax.inject.Inject;

import org.eclipse.sisu.peaberry.builders.ProxyProvider;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Multiple dynamic proxies and direct services {@link Provider}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class MultipleServiceProvider<T>
    implements ProxyProvider<Iterable<T>> {

  @Inject
  Injector injector;

  private final ServiceSettings<T> setup;
  private final Class<T> clazz;

  MultipleServiceProvider(final ServiceSettings<T> setup) {
    // clone current state of settings
    this.setup = setup.clone();
    this.clazz = setup.getClazz();
  }

  public Iterable<T> get() {
    return serviceProxies(clazz, setup.getImports(injector, false), setup.getDecorator(injector));
  }

  private static final class DirectProvider<T>
      implements Provider<Iterable<T>> {

    @Inject
    Injector injector;

    private final ServiceSettings<T> setup;

    DirectProvider(final ServiceSettings<T> setup) {
      // settings already cloned
      this.setup = setup;
    }

    public Iterable<T> get() {
      return directServices(setup.getImports(injector, false), setup.getDecorator(injector));
    }
  }

  public Provider<Iterable<T>> direct() {
    return new DirectProvider<>(setup);
  }
}
