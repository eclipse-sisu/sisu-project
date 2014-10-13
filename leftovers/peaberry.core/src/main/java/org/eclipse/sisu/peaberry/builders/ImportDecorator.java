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

import org.eclipse.sisu.peaberry.Import;

/**
 * Provide runtime decoration/intercepting of imported services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface ImportDecorator<S> {

  /**
   * Decorate the given imported service.
   * 
   * @param service imported service handle
   * @return decorated service handle
   */
  <T extends S> Import<T> decorate(Import<T> service);
}
