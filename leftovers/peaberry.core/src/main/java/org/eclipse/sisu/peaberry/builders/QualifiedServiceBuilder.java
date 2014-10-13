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

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.Peaberry;

import com.google.inject.Key;

/**
 * See {@link Peaberry} for examples of the dynamic service builder API.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface QualifiedServiceBuilder<T>
    extends InjectedServiceBuilder<T> {

  /**
   * Apply the given attributes to the dynamic service.
   * 
   * @param key service attributes key
   * @return dynamic service builder
   */
  InjectedServiceBuilder<T> attributes(Key<? extends Map<String, ?>> key);

  /**
   * Apply the given attributes to the dynamic service.
   * 
   * @param instance service attributes
   * @return dynamic service builder
   */
  InjectedServiceBuilder<T> attributes(Map<String, ?> instance);

  /**
   * Apply the given filter to the dynamic service.
   * 
   * @param key attribute filter key
   * @return dynamic service builder
   */
  InjectedServiceBuilder<T> filter(Key<? extends AttributeFilter> key);

  /**
   * Apply the given filter to the dynamic service.
   * 
   * @param instance attribute filter
   * @return dynamic service builder
   */
  InjectedServiceBuilder<T> filter(AttributeFilter instance);
}
