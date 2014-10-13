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

package org.eclipse.sisu.peaberry.eclipse;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this to map a bean type to a particular Extension Point.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ExtensionBean {

  /**
   * if true then the Extension elements will be combined into one bean
   */
  boolean aggregate() default false;

  /**
   * the unique identifier of the Extension Point
   */
  String value();
}
