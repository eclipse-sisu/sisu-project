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
 * Reusable classes for the implementation of an OSGi Extender that maintains a 
 * a dynamic {@link com.google.inject.Injector} graph by scanning bundles as they come and go.
 * <p>
 * The classes in this package are used by the default Sisu extender but can be used for the 
 * implementation of custom extenders as well.
 * </p>
 * <p>
 * A typical extender would be implemented as follows:
 * <ol>
 * <li>Create a singleton instance of a {@link MutableBeanLocator}</li>
 * <li>Create a {@link org.osgi.util.tracker.BundleTracker} using customizer {@link ModuleBundleTracker}</li>
 * <li>Create a {@link org.osgi.util.tracker.ServiceTracker} using customizer {@link PublisherServiceTracker}</li>
 * </ol>
 * <p>
 * Each time an extendable bundle appears in the OSGi framework and is selected by the {@link BundleSelector} in use, 
 * the {@link ModuleBundleTracker} creates an instance of {@link BindingPublisher} and 
 * registers it in the OSGi registry. The created {@link BindingPublisher} is actually an instance of {@link org.eclipse.sisu.inject.InjectorPublisher} which is passed
 * a new Guice Injector backed by a Sisu {@link WireModule}
 * and a {@link org.eclipse.sisu.space.BundleClassSpace} created after the extended bundle. The {@link WireModule}
 * is also passed a {@link org.eclipse.sisu.osgi.extender.BundleModule}, a Guice module created using the extended
 * bundle that binds the singleton {@link MutableBeanLocator} instance.
 * </p>
 * <p>
 * The {@link PublisherServiceTracker} detects the presence of the new {@link BindingPublisher} and
 * uses it to extend the {@link MutableBeanLocator}. Similarly, when the extended bundle goes offline and its {@link BindingPublisher} is disposed,
 * the {@link PublisherServiceTracker} removes the {@link BindingPublisher} from the {@link MutableBeanLocator}. In short, the {@link PublisherServiceTracker} is used
 * as convenient way of keeping the shared {@link MutableBeanLocator} up to date. 
 * </p>
 * <p>
 * When the Sisu extender is used, user bundles can declare additional Guice modules by including a file named <code>com.google.inject.Module</code> in folder
 * <code>META-INF/services/</code>. The file must contain the fully qualified name of a Guice module implementation class. The above file will be discovered by Sisu
 * using {@link org.eclipse.sisu.launch.SisuExtensions}, see the implementation of {@link BundleModule} for details.
 * </p>
 * When the Sisu extender is used, user bundles can also extend Sisu programmatically, by registering in the OSGi service registry custom instances of {@link BindingPublisher}. 
 * {@link InjectorPublisher} can be used for the purpose, but it's important that the service is registered using proper metadata for Sisu to collect.
 * See {@link ModuleBundleTracker} Javadoc for details.
 */
package org.eclipse.sisu.osgi.extender;

