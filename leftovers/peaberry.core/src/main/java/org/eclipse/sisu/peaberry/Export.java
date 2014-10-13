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

package org.eclipse.sisu.peaberry;

import java.util.Map;

/**
 * Handle to a service instance exported to a {@link ServiceWatcher}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface Export<T>
    extends Import<T> {

  /**
   * Replace the exported service with the given instance.
   * 
   * @param instance service instance
   */
  void put(T instance);

  /**
   * Update the attributes associated with the exported service.
   * 
   * @param attributes service attributes
   */
  void attributes(Map<String, ?> attributes);

  /**
   * Remove the exported service from the {@link ServiceWatcher}.
   */
  void unput();
}
