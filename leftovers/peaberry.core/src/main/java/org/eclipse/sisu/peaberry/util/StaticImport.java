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

import static java.util.Collections.unmodifiableMap;

import java.util.Map;

import org.eclipse.sisu.peaberry.Import;

/**
 * An {@link Import} consisting of a fixed instance and optional attribute map.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class StaticImport<T>
    implements Import<T> {

  private final T instance;
  private final Map<String, ?> attributes;

  /**
   * Create a static {@link Import} for the given instance.
   * 
   * @param instance service instance
   */
  public StaticImport(final T instance) {
    this.instance = instance;
    this.attributes = null;
  }

  /**
   * Create a static {@link Import} for the given instance and attribute map.
   * 
   * @param instance service instance
   * @param attributes service attributes
   */
  public StaticImport(final T instance, final Map<String, ?> attributes) {
    this.instance = instance;
    this.attributes = null == attributes ? null : unmodifiableMap(attributes);
  }

  public T get() {
    return instance;
  }

  public Map<String, ?> attributes() {
    return attributes;
  }

  public void unget() {/* nothing to do */}

  public boolean available() {
    return null != instance;
  }
}
