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

/**
 * A service watcher can receive services provided by other registries.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 * 
 * @since 1.1
 */
public interface ServiceWatcher<S> {

  /**
   * Add the given service to this watcher.
   * 
   * @param service imported service handle
   * @return exported service handle, null if the watcher is not interested
   */
  <T extends S> Export<T> add(Import<T> service);
}
