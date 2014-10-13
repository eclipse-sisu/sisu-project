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

import org.eclipse.sisu.peaberry.ServiceRegistry;

/**
 * {@link ServiceRegistry} that caches service instances for faster lookup.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface CachingServiceRegistry
    extends ServiceRegistry {

  /**
   * Release any unused service instances from the cache.
   * 
   * @param targetGeneration the generation to flush
   */
  void flush(int targetGeneration);
}
