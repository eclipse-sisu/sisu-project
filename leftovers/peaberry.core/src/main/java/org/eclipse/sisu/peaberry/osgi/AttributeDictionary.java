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

package org.eclipse.sisu.peaberry.osgi;

import static java.util.Collections.enumeration;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

/**
 * Lazy read-only {@link Dictionary} backed by a service attribute map.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class AttributeDictionary
    extends Dictionary<String, Object> {

  private final Map<String, Object> attributes;

  @SuppressWarnings("unchecked")
  public AttributeDictionary(final Map<String, ?> attributes) {
    this.attributes = (Map) attributes;
  }

  @Override
  public Object get(final Object key) {
    return attributes.get(key);
  }

  @Override
  public Enumeration<String> keys() {
    return enumeration(attributes.keySet());
  }

  @Override
  public boolean isEmpty() {
    return attributes.isEmpty();
  }

  @Override
  public int size() {
    return attributes.size();
  }

  @Override
  public Enumeration<Object> elements() {
    return enumeration(attributes.values());
  }

  @Override
  public Object put(final String key, final Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object remove(final Object key) {
    throw new UnsupportedOperationException();
  }
}
