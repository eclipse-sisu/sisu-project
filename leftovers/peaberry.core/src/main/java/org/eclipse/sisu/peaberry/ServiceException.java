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
 * General purpose service exception.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public class ServiceException
    extends RuntimeException {

  private static final long serialVersionUID = -1744260261446638374L;

  /**
   * Constructs a {@code ServiceException} with no message or cause.
   */
  public ServiceException() {
    super();
  }

  /**
   * Constructs a {@code ServiceException} with a specific message.
   * 
   * @param message detailed message
   */
  public ServiceException(final String message) {
    super(message);
  }

  /**
   * Constructs a {@code ServiceException} with a specific cause.
   * 
   * @param cause underlying cause
   */
  public ServiceException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code ServiceException} with message and cause.
   * 
   * @param message detailed message
   * @param cause underlying cause
   */
  public ServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
