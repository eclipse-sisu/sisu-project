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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceUnavailableException;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;

/**
 * Factory methods for direct (also known as static) services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class DirectServiceFactory {

  // instances not allowed
  private DirectServiceFactory() {}

  static <T> Iterable<T> directServices(final Iterable<Import<T>> services,
      final ImportDecorator<? super T> decorator) {

    final List<T> instances = new ArrayList<T>();
    final Iterator<Import<T>> i = services.iterator();

    while (i.hasNext()) {
      // collect all valid services into snapshot list
      final T instance = nextService(i, decorator);
      if (null != instance) {
        instances.add(instance);
      }
    }

    return unmodifiableList(instances);
  }

  static <T> T directService(final Iterable<Import<T>> services,
      final ImportDecorator<? super T> decorator) {

    final Iterator<Import<T>> i = services.iterator();

    while (i.hasNext()) {
      // return the first valid service found
      final T instance = nextService(i, decorator);
      if (null != instance) {
        return instance;
      }
    }

    return null;
  }

  private static <T> T nextService(final Iterator<Import<T>> i,
      final ImportDecorator<? super T> decorator) {

    try {
      return (null == decorator ? i.next() : decorator.decorate(i.next())).get();
    } catch (final ServiceUnavailableException e) {/* default to null */} // NOPMD

    return null;
  }
}
