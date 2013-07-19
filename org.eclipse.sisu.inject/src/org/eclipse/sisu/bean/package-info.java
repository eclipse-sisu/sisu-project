/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * Customizable injection of bean properties.
 * <p><p>
 * For example:
 * 
 * <pre>
 * new AbstractModule() {
 *   &#064;Override
 *   protected void configure() {
 *     bindListener( Matchers.any(), new BeanListener( new MyBeanBinder() ) );
 *   }
 * }</pre>
 * MyBeanBinder will be asked to supply a {@link org.eclipse.sisu.bean.PropertyBinder} per-bean type.
 * <p><p>
 * Each PropertyBinder will in turn be asked for a {@link org.eclipse.sisu.bean.PropertyBinding} per-property.
 * <p><p>
 * The PropertyBindings are used to set values in any injected instances of the bean.
 */
package org.eclipse.sisu.bean;

