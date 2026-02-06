/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
/**
 * Customizable injection of bean properties, based on <a href="https://github.com/google/guice/wiki/CustomInjections">https://github.com/google/guice/wiki/CustomInjections</a>.
 * <p>
 * For example:
 *
 * <pre>
 * new AbstractModule() {
 *   &#064;Override protected void configure() {
 *     bindListener( Matchers.any(), new BeanListener( new MyBeanBinder() ) );
 *   }
 * }</pre>
 * {@code MyBeanBinder} will be asked to supply a {@link org.eclipse.sisu.bean.PropertyBinder} for each bean type, say Foo.
 * <p>
 * That {@code PropertyBinder} will be asked to supply a {@link org.eclipse.sisu.bean.PropertyBinding} for each property (field or setter) in Foo.
 * <p>
 * Those {@code PropertyBinding}s are then used to automatically configure any injected instances of Foo.
 */
package org.eclipse.sisu.bean;
