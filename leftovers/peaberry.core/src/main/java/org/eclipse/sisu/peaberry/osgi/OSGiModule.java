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

package org.eclipse.sisu.peaberry.osgi;

import org.eclipse.sisu.peaberry.BundleScoped;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.cache.CachingServiceRegistry;
import org.eclipse.sisu.peaberry.cache.Chain;
import org.eclipse.sisu.peaberry.cache.RegistryChain;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * OSGi specific Guice binding {@link Module}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class OSGiModule
    extends AbstractModule {

  private final BundleContext bundleContext;
  private final ServiceRegistry[] registries;

  public OSGiModule(final BundleContext bundleContext, final ServiceRegistry... registries) {
    if (null == bundleContext) {
      throw new IllegalArgumentException("null bundle context");
    }

    this.bundleContext = bundleContext;
    this.registries = registries;
  }

  public OSGiModule(final ServiceRegistry... registries) {
      this.bundleContext = null;
      this.registries = registries;
  }

  @Override
  protected void configure() {
    if (bundleContext != null) {
      bind(BundleContext.class).toInstance(bundleContext);
    }

    bindScope(BundleScoped.class, new BundleScopeImpl(getProvider(BundleContext.class)));

    if (registries.length == 0) {
      bind(ServiceRegistry.class).to(CachingServiceRegistry.class);
    } else {
      bind(ServiceRegistry.class).annotatedWith(Chain.class).to(CachingServiceRegistry.class);
      bind(ServiceRegistry[].class).annotatedWith(Chain.class).toInstance(registries);
      bind(ServiceRegistry.class).to(RegistryChain.class);
    }

    // the binding key class will be used as the bundle-scoped service API
    bind(CachingServiceRegistry.class).to(OSGiServiceRegistry.class).in(BundleScoped.class);
  }
}
