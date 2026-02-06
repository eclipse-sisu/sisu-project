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

import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import org.eclipse.sisu.peaberry.ServiceException;
import org.eclipse.sisu.peaberry.cache.AbstractServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Keep track of imported OSGi services that provide a specific interface.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class OSGiServiceListener<T>
    extends AbstractServiceListener<T>
    implements ServiceListener {

  private final BundleContext bundleContext;
  private final String ldapFilter;

  OSGiServiceListener(final BundleContext bundleContext, final String ldapFilter) {
    this.bundleContext = bundleContext;
    this.ldapFilter = ldapFilter;
  }

  @Override
  protected synchronized void start() {
    try {

      // register listener first to avoid race condition
      bundleContext.addServiceListener(this, ldapFilter);

      // retrieve snapshot of any matching services that are already registered
      final ServiceReference[] refs = bundleContext.getServiceReferences(null, ldapFilter);
      if (null != refs) {

        // wrap service references to optimize sorting
        for (final ServiceReference r : refs) {
          insertService(new OSGiServiceImport<T>(bundleContext, r)); // NOSONAR
        }
      }

    } catch (final InvalidSyntaxException e) {
      throw new ServiceException(e); // this should never happen!
    }
  }

  public synchronized void serviceChanged(final ServiceEvent event) {

    final OSGiServiceImport<T> service =
        new OSGiServiceImport<T>(bundleContext, event.getServiceReference());

    switch (event.getType()) {
    case REGISTERED:
      insertService(service);
      break;
    case MODIFIED:
      updateService(service);
      break;
    case UNREGISTERING:
      removeService(service);
      break;
    default:
      break;
    }
  }
}
