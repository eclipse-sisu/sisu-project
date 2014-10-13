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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.sisu.peaberry.cache.ImmutableAttribute;
import org.osgi.framework.ServiceReference;

/**
 * Service attributes adapter backed by an OSGi {@link ServiceReference}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class OSGiServiceAttributes
    extends AbstractMap<String, Object> {

  final ServiceReference ref;

  OSGiServiceAttributes(final ServiceReference ref) {
    this.ref = ref;
  }

  @Override
  public Object get(final Object key) {
    return ref.getProperty((String) key);
  }

  // can safely cache entry set, as it has no state
  private volatile Set<Entry<String, Object>> entrySet;

  @Override
  public Set<Entry<String, Object>> entrySet() {
    if (null == entrySet) {
      entrySet = new AbstractSet<Entry<String, Object>>() {

        @Override
        public Iterator<Entry<String, Object>> iterator() {

          // take snapshot of current property names
          final String[] keys = ref.getPropertyKeys();

          return new Iterator<Entry<String, Object>>() {
            private int i = 0;

            public boolean hasNext() {
              return i < keys.length;
            }

            public Entry<String, Object> next() {
              final String k = keys[i++];

              // return a snapshot of the current property entry
              return new ImmutableAttribute(k, ref.getProperty(k));
            }

            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }

        @Override
        public int size() {
          return ref.getPropertyKeys().length;
        }
      };
    }

    return entrySet;
  }
}
