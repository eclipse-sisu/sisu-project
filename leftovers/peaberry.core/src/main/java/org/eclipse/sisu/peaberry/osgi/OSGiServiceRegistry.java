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

import static org.eclipse.sisu.peaberry.Peaberry.NATIVE_FILTER_HINT;

import javax.inject.Inject;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.cache.AbstractServiceListener;
import org.eclipse.sisu.peaberry.cache.AbstractServiceRegistry;
import org.osgi.framework.BundleContext;

/**
 * OSGi {@link ServiceRegistry} implementation.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public class OSGiServiceRegistry
    extends AbstractServiceRegistry {

  private final BundleContext bundleContext;

  @Inject
  public OSGiServiceRegistry(final BundleContext bundleContext) {
    super(Boolean.parseBoolean(bundleContext.getProperty(NATIVE_FILTER_HINT)));
    this.bundleContext = bundleContext;
  }

  @Override
  public final String toString() {
    return String.format("OSGiServiceRegistry[%s]", bundleContext.getBundle());
  }

  @Override
  public final int hashCode() {
    return bundleContext.hashCode();
  }

  @Override
  public final boolean equals(final Object rhs) {
    if (rhs instanceof OSGiServiceRegistry) {
      return bundleContext.equals(((OSGiServiceRegistry) rhs).bundleContext);
    }
    return false;
  }

  @Override
  protected final <T> AbstractServiceListener<T> createListener(final String ldapFilter) {
    return new OSGiServiceListener<T>(bundleContext, ldapFilter);
  }

  public final <T> Export<T> add(final Import<T> service) {
    // avoid cycles by ignoring our own services
    if (service instanceof OSGiServiceExport<?>) {
      return null;
    }
    return new OSGiServiceExport<T>(bundleContext, service);
  }
}
