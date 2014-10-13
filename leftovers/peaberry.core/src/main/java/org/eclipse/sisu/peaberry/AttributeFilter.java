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
 * Simple matching filter for service attributes.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public interface AttributeFilter {

  /**
   * Tests whether or not this filter matches the given service attributes.
   * 
   * @param attributes service attributes
   * @return true if the given attributes match this filter, otherwise false
   */
  boolean matches(Map<String, ?> attributes);
}
