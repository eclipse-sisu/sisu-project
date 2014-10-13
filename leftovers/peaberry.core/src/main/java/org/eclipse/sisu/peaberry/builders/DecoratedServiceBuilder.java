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

package org.eclipse.sisu.peaberry.builders;

import org.eclipse.sisu.peaberry.Peaberry;

import com.google.inject.Key;

/**
 * See {@link Peaberry} for examples of the dynamic service builder API.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface DecoratedServiceBuilder<T>
    extends QualifiedServiceBuilder<T> {

  /**
   * Apply the given decoration to the dynamic service.
   * 
   * @param key decorator key
   * @return dynamic service builder
   */
  QualifiedServiceBuilder<T> decoratedWith(Key<? extends ImportDecorator<? super T>> key);

  /**
   * Apply the given decoration to the dynamic service.
   * 
   * @param instance decorator
   * @return dynamic service builder
   */
  QualifiedServiceBuilder<T> decoratedWith(ImportDecorator<? super T> instance);
}
