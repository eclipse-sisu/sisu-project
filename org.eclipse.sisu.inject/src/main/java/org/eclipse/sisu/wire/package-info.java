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
 * Customizable wiring of unresolved dependencies. Use this to share components across injectors, apply configuration, and form on-demand collections.
 * <p>
 * The {@link org.eclipse.sisu.wire.WireModule} should enclose all modules in your application:
 *
 * <pre>
 * Guice.createInjector( new WireModule( bootModule, configModule, mainModule ) );</pre>
 *
 * Use the {@link org.eclipse.sisu.wire.ChildWireModule} when you want to wire child injectors:
 *
 * <pre>
 * injector.createChildInjector( new ChildWireModule( serviceModule, subModule ) );</pre>
 * <hr>
 * The default wiring strategy is to use {@link org.eclipse.sisu.wire.LocatorWiring} which can supply the following bindings via the {@link org.eclipse.sisu.inject.BeanLocator}:
 *
 * <p><strong>Instances</strong></p>
 * <pre>
 * &#064;Inject MyType bean
 *
 * &#064;Inject &#064;Named("hint") MyType namedBean
 *
 * &#064;Inject &#064;MyQualifier MyType qualifiedBean
 *
 * &#064;Inject Provider&lt;MyType&gt; beanProvider</pre>
 *
 * <p><strong>Configuration</strong></p>
 * <pre>
 * &#064;Inject &#064;Named("${my.property.name}") File file                      // supports basic type conversion
 *
 * &#064;Inject &#064;Named("${my.property.name:-http://example.org/}") URL url   // can give default in case property is not set
 *
 * &#064;Inject &#064;Named("${my.property.name:-development}") MyType bean       // can be used to pick specific &#064;Named beans
 *
 * &#064;Inject &#064;Named("my.property.name") int port                          // shorthand syntax</pre>
 * <p>
 * You can bind your configuration at runtime as follows:
 * <pre>
 * bind( {@link org.eclipse.sisu.wire.ParameterKeys#PROPERTIES ParameterKeys.PROPERTIES} ).toInstance( myConfiguration );      // multiple bindings are merged into one view</pre>
 *
 * <p><strong>Collections</strong></p>
 * The following collections are both dynamic and thread-safe, elements may come and go as injectors are added or removed from the {@link org.eclipse.sisu.inject.BeanLocator}.
 * <p>
 * They are also <b>lazy</b>, meaning instances are created as you access elements of the collection; the elements are then re-used for the same collection.
 *
 * <pre>
 * &#064;Inject List&lt;MyType&gt; list
 *
 * &#064;Inject List&lt;Provider&lt;MyType&gt;&gt; providers
 *
 * &#064;Inject Iterable&lt;{@link org.eclipse.sisu.BeanEntry}&lt;MyQualifier, MyType&gt;&gt; entries             // gives access to additional metadata</pre>
 *
 * <pre>
 * &#064;Inject Map&lt;String, MyType&gt; stringMap                                // strings are taken from @Named values
 *
 * &#064;Inject Map&lt;Named, MyType&gt; namedMap
 *
 * &#064;Inject Map&lt;MyQualifier, MyType&gt; qualifiedMap
 *
 * &#064;Inject Map&lt;String, Provider&lt;MyType&gt;&gt; providerMap</pre>
 */
package org.eclipse.sisu.wire;
