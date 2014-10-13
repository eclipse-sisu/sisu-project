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
import org.eclipse.sisu.peaberry.builders.ImportDecorator;

/**
 * Skeletal implementation to simplify development of {@link ImportDecorator}s.
 * <p>
 * Developers only have to extend this class and provide an implementation of
 * the {@link #decorate(Object, Map)} method, which takes the original service
 * instance and associated attribute map, and returns a decorated instance.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public abstract class AbstractDecorator<S>
    implements ImportDecorator<S> {

  public final <T extends S> Import<T> decorate(final Import<T> service) {
    return new DelegatingImport<T>(service) {

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        return (T) decorate(service.get(), service.attributes());
      }
    };
  }

  /**
   * Decorate the current service instance.
   * 
   * @param instance service instance
   * @param attributes service attributes
   * @return decorated service instance
   */
  protected abstract S decorate(S instance, Map<String, ?> attributes);
}
