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
import org.eclipse.sisu.peaberry.ServiceRegistry;

import com.google.inject.Key;

/**
 * See {@link Peaberry} for examples of the dynamic service builder API.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface InjectedServiceBuilder<T>
    extends OutjectedServiceBuilder<T> {

  /**
   * Inject the dynamic service from a specific registry.
   * 
   * @param key service registry key
   * @return dynamic service builder
   */
  OutjectedServiceBuilder<T> in(Key<? extends ServiceRegistry> key);

  /**
   * Inject the dynamic service from a specific registry.
   * 
   * @param instance service registry
   * @return dynamic service builder
   */
  OutjectedServiceBuilder<T> in(ServiceRegistry instance);
}
