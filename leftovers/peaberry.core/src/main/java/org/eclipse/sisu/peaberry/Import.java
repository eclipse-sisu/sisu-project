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
 * Handle to a service instance imported from a {@link ServiceRegistry}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface Import<T> {

  /**
   * Start using the imported service instance.
   * 
   * @return service instance
   * 
   * @throws ServiceUnavailableException if the service is unavailable
   */
  T get();

  /**
   * Get the attributes associated with the service.
   * 
   * @return current attribute map
   */
  Map<String, ?> attributes();

  /**
   * Stop using the imported service instance.
   */
  void unget();

  /**
   * Is the service instance available?
   * 
   * @return true if the service is available, otherwise false
   * 
   * @since 1.1
   */
  boolean available();
}
