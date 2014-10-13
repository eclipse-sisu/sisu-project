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

import org.eclipse.sisu.peaberry.Import;

/**
 * A simple {@link Import} that always delegates to another {@link Import}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
@SuppressWarnings("PMD.AbstractNaming")
public abstract class DelegatingImport<T>
    implements Import<T> {

  private final Import<T> service;

  /**
   * Create a new {@link Import} that delegates to the given {@link Import}.
   * 
   * @param service delegate service
   */
  protected DelegatingImport(final Import<T> service) {
    this.service = service;
  }

  public T get() {
    return service.get();
  }

  public Map<String, ?> attributes() {
    return service.attributes();
  }

  public void unget() {
    service.unget();
  }

  public boolean available() {
    return service.available();
  }
}
