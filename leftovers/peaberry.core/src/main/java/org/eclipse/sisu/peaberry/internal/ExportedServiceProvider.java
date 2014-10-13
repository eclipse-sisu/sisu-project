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

import javax.inject.Inject;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.builders.ExportProvider;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Exported handle and direct service {@link Provider}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ExportedServiceProvider<T>
    implements ExportProvider<T> {

  @Inject
  Injector injector;

  private final ServiceSettings<T> setup;

  ExportedServiceProvider(final ServiceSettings<T> setup) {
    // clone current state of settings
    this.setup = setup.clone();
  }

  public Export<T> get() {
    return setup.getExport(injector);
  }

  private static final class DirectProvider<T>
      implements Provider<T> {

    @Inject
    Injector injector;

    private final ServiceSettings<T> setup;

    DirectProvider(final ServiceSettings<T> setup) {
      // settings already cloned
      this.setup = setup;
    }

    public T get() {
      return setup.getExport(injector).get();
    }
  }

  public Provider<T> direct() {
    return new DirectProvider<T>(setup);
  }
}
