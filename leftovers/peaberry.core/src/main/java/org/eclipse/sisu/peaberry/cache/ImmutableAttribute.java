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

import java.util.Map.Entry;

/**
 * Snapshot of an entry from a service attribute map.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class ImmutableAttribute
    implements Entry<String, Object> {

  private final String k;
  private final Object v;

  public ImmutableAttribute(final String key, final Object value) {
    k = key;
    v = value;
  }

  public String getKey() {
    return k;
  }

  public Object getValue() {
    return v;
  }

  public Object setValue(final Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object rhs) {
    if (rhs instanceof Entry) {
      final Entry entry = (Entry) rhs;
      return equals(k, entry.getKey()) && equals(v, entry.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (null == k ? 0 : k.hashCode()) ^ (null == v ? 0 : v.hashCode());
  }

  private static boolean equals(final Object lhs, final Object rhs) {
    return null == lhs ? null == rhs : lhs.equals(rhs);
  }
}
