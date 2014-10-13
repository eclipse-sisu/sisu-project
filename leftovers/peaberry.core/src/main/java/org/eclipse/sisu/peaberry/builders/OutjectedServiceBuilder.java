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
import org.eclipse.sisu.peaberry.ServiceWatcher;

import com.google.inject.Key;

/**
 * See {@link Peaberry} for examples of the dynamic service builder API.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface OutjectedServiceBuilder<T>
    extends ServiceBuilder<T> {

  /**
   * Outject the dynamic service to the given watcher.
   * 
   * @param key service watcher key
   * @return dynamic service builder
   */
  ServiceBuilder<T> out(Key<? extends ServiceWatcher<? super T>> key);

  /**
   * Outject the dynamic service to the given watcher.
   * 
   * @param instance service watcher
   * @return dynamic service builder
   */
  ServiceBuilder<T> out(ServiceWatcher<? super T> instance);
}
