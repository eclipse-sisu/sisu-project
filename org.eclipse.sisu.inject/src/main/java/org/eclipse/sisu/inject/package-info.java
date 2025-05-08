/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
 * Dynamic bean lookup across multiple injectors.
 * <p>
 * The {@link org.eclipse.sisu.inject.BeanLocator} lets you lookup and keep watch for bean implementations;
 * it does this by processing binding information from one or more {@code BindingPublisher}s, such as injectors.
 * <p>
 * You can add or remove {@link org.eclipse.sisu.inject.BindingPublisher}s using the {@link org.eclipse.sisu.inject.MutableBeanLocator}
 * view; any existing watchers or returned collections are updated to reflect the latest binding information.
 * <p>
 * {@link org.eclipse.sisu.inject.DefaultBeanLocator} will automatically add any injectors it's bound in by virtue
 * of an injected setter. This makes it easy to share across multiple injectors with a simple instance binding: 
 * 
 * <pre>
 * Module locatorModule = new AbstractModule() {
 *   private final DefaultBeanLocator locator = new DefaultBeanLocator();
 *   
 *   &#064;Override protected void configure() {
 *     bind( DefaultBeanLocator.class ).toInstance( locator );
 *   }
 * };
 * 
 * Injector injectorA = Guice.createInjector( new WireModule( locatorModule, spaceModuleA ) );   // adds injectorA to locator
 * Injector injectorB = Guice.createInjector( new WireModule( locatorModule, spaceModuleB ) );   // adds injectorB to locator</pre>
 * 
 * If you want to use a {@code DefaultBeanLocator} in a given injector, but don't want that injector
 * added automatically, wrap the locator inside a provider to hide the injected setter from Guice:
 * 
 * <pre>
 * bind( DefaultBeanLocator.class ).toProvider( Providers.of( locator ) );</pre>
 * 
 * By default all bindings in an injector are separated into two partitions (default vs non-default)
 * and ranked according to their sequence number. This is so bindings from multiple injectors can be
 * interleaved to keep default components prioritized before non-default, while still maintaining an
 * overall ordering between injectors. To override the default bind your own {@link org.eclipse.sisu.inject.RankingFunction}:
 * 
 * <pre>
 * bind( RankingFunction.class ).to( MyRankingFunction.class );</pre>
 */
package org.eclipse.sisu.inject;
