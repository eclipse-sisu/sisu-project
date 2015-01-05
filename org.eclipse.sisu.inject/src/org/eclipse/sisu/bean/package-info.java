/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * Customizable injection of bean properties, based on <a href="http://code.google.com/p/google-guice/wiki/CustomInjections">http://code.google.com/p/google-guice/wiki/CustomInjections</a>.
 * <p><p>
 * For example:
 * 
 * <pre>
 * new AbstractModule() {
 *   &#064;Override protected void configure() {
 *     bindListener( Matchers.any(), new BeanListener( new MyBeanBinder() ) );
 *   }
 * }</pre>
 * {@code MyBeanBinder} will be asked to supply a {@link org.eclipse.sisu.bean.PropertyBinder} for each bean type, say Foo.
 * <p><p>
 * That {@code PropertyBinder} will be asked to supply a {@link org.eclipse.sisu.bean.PropertyBinding} for each property (field or setter) in Foo.
 * <p><p>
 * Those {@code PropertyBinding}s are then used to automatically configure any injected instances of Foo.
 */
package org.eclipse.sisu.bean;

