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
 * Customizable scanning of bean implementations.
 * <p>
 * The {@link org.eclipse.sisu.space.SpaceModule} should be given a {@link org.eclipse.sisu.space.ClassSpace} representing the classes and resources to scan:
 * 
 * <pre>
 * Guice.createInjector( new SpaceModule( new URLClassSpace( classloader ) ) );</pre>
 * 
 * Reduce scanning time by using an {@link org.eclipse.sisu.space.SisuIndex index} or provide your own {@link org.eclipse.sisu.space.ClassFinder} approach:
 * 
 * <pre>
 * Guice.createInjector( new SpaceModule( new URLClassSpace( classloader ), BeanScanning.INDEX ) );</pre>
 * <hr>
 * The default visitor strategy is to use {@link org.eclipse.sisu.space.QualifiedTypeVisitor} with {@link org.eclipse.sisu.space.QualifiedTypeBinder} to find types annotated with {@code @Named} or other {@code @Qualifier}s and bind them as follows:
 * 
 * <p><strong>Components</strong></p>
 * Any qualified components are bound using a special "wildcard" key that the {@link org.eclipse.sisu.inject.BeanLocator} uses to check type compatibility at lookup time:
 * <p>
 * (This avoids the need to walk the type hierarchy and register bindings for each and every super-type, turning the injector graph to spaghetti.)
 * 
 * <pre>
 * &#064;Named("example") public class MyTypeImpl implements MyType {
 *   // ...
 * }</pre>
 * If you use an empty {@code @Named} or a different {@code @Qualifier} annotation then Sisu will pick a canonical name based on the implementation type.
 * 
 * <p>
 * Sometimes you need explicit typed bindings for external integration; you can list the types in a {@code @Typed} annotation or leave it empty to use the declared interfaces:
 * 
 * <pre>
 * &#064;Named &#064;Typed public class MyTypeImpl implements MyType {
 *   // ...
 * }</pre>
 * 
 * Default implementations can be indicated by using "default" as a binding name:
 * 
 * <pre>
 * &#064;Named("default") public class MyTypeImpl implements MyType {
 *   // ...
 * }</pre>
 * 
 * or by starting the implementation name with "Default":
 * 
 * <pre>
 * &#064;Named public class DefaultMyType implements MyType {
 *   // ...
 * }</pre>
 * 
 * Default components are bound without a qualifier and have a higher ranking than non-default components.
 * 
 * <p><strong>Providers</strong></p>
 * Any qualified providers are bound using the same binding heuristics as components:
 * 
 * <pre>
 * &#064;Named public class MyProvider implements Provider&lt;MyType&gt; {
 *   public MyType get() {
 *     // ...
 *   }
 * }</pre>
 * Use {@code @Singleton} to scope the provided binding(s) as a singleton:
 * 
 * <pre>
 * &#064;Named &#064;Singleton public class MyProvider implements Provider&lt;MyType&gt; {
 *   public MyType get() {
 *     // ...
 *   }
 * }</pre>
 * Note: this is different to the normal Guice behaviour where singleton only applies to the provider itself.
 * 
 * <p><strong>Modules</strong></p>
 * Any qualified modules are are installed using the current binder:
 * 
 * <pre>
 * &#064;Named public class MyModule extends AbstractModule {
 *   &#064;Override protected void configure() {
 *     // ...
 *   }
 * }</pre>
 * 
 * <p><strong>Mediators</strong></p>
 * Any qualified {@link org.eclipse.sisu.Mediator}s are registered with the {@link org.eclipse.sisu.inject.BeanLocator}:
 * 
 * <pre>
 * &#064;Named public class MyMediator implements Mediator&lt;Named, MyType, MyWatcher&gt; {
 *   public void add( BeanEntry&lt;Named, MyType&gt; entry, MyWatcher watcher ) throws Exception {
 *     // ...
 *   }
 * 
 *   public void remove( BeanEntry&lt;Named, MyType&gt; entry, MyWatcher watcher ) throws Exception {
 *     // ...
 *   }
 * }</pre>
 */
package org.eclipse.sisu.space;
