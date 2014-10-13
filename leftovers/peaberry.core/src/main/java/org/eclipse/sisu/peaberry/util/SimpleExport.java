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

package org.eclipse.sisu.peaberry.util;

import java.util.Map;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;

/**
 * A basic mutable {@link Export} derived from a single dynamic {@link Import}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public class SimpleExport<T>
    implements Export<T> {

  // track usage of the original dynamic service so we can unwind it later
  private static final class CountingImport<T>
      extends DelegatingImport<T> {

    private int count;

    CountingImport(final Import<T> service) {
      super(service);
    }

    @Override
    public T get() {
      count++;
      return super.get();
    }

    @Override
    public void unget() {
      count--;
      super.unget();
    }

    void unwind() {
      // service swapped while still in use, so balance the surplus of "gets"
      while (count-- > 0) {
        super.unget();
      }
    }
  }

  private Import<T> service;
  private Map<String, ?> attributes;

  /**
   * Create a new {@link Export} from the given {@link Import}.
   * 
   * @param service service being exported
   */
  public SimpleExport(final Import<T> service) {
    this.service = new CountingImport<T>(service);
  }

  public synchronized T get() {
    return service.get();
  }

  public synchronized Map<String, ?> attributes() {
    return null == attributes ? service.attributes() : attributes;
  }

  public synchronized void unget() {
    service.unget();
  }

  public boolean available() {
    return service.available();
  }

  public synchronized void put(final T newInstance) {
    // might need to balance the gets + ungets
    if (service instanceof CountingImport<?>) {
      ((CountingImport<?>) service).unwind();
    }

    service = new StaticImport<T>(newInstance, service.attributes());
  }

  public synchronized void attributes(final Map<String, ?> newAttributes) {
    attributes = newAttributes;
  }

  public void unput() {
    put(null); // simple alias
  }
}
