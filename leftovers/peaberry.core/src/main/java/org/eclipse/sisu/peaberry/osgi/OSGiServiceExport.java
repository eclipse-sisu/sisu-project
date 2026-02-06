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

import static org.osgi.framework.Constants.OBJECTCLASS;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.util.SimpleExport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link Export} implementation backed by an OSGi {@link ServiceRegistration}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class OSGiServiceExport<T>
    extends SimpleExport<T> {

  private final BundleContext bundleContext;
  private ServiceRegistration reg;

  OSGiServiceExport(final BundleContext bundleContext, final Import<T> service) {
    super(service);
    this.bundleContext = bundleContext;
    exportOSGiService();
  }

  @Override
  public synchronized void put(final T newInstance) {
    removeOSGiService();
    super.put(newInstance);
    exportOSGiService();
  }

  @Override
  public synchronized void attributes(final Map<String, ?> newAttributes) {
    super.attributes(newAttributes);
    if (null != reg) {
      reg.setProperties(getProperties(newAttributes));
    }
  }

  private void exportOSGiService() {
    final T instance = get();
    if (null != instance) {
      final Dictionary<String, ?> properties = getProperties(attributes());
      final String[] interfaceNames = getInterfaceNames(instance, properties);
      reg = bundleContext.registerService(interfaceNames, instance, properties);
    }
  }

  private void removeOSGiService() {
    if (null != reg) {
      try {
        reg.unregister();
      } catch (final RuntimeException re) {/* already gone */} // NOSONAR
      reg = null;
    }
  }

  private static Dictionary<String, ?> getProperties(final Map<String, ?> attributes) {
    return null == attributes || attributes.isEmpty() ? null : new AttributeDictionary(attributes);
  }

  private static String[] getInterfaceNames(final Object instance, final Dictionary<?, ?> properties) {
    final Object objectClass = null == properties ? null : properties.get(OBJECTCLASS);

    // check service attributes setting
    if (objectClass instanceof String[]) {
      return (String[]) objectClass;
    }

    final Set<String> names = new HashSet<String>();

    for (Class<?> clazz = instance.getClass(); null != clazz; clazz = clazz.getSuperclass()) {
      for (final Class<?> i : clazz.getInterfaces()) {
        names.add(i.getName());
      }
    }

    return names.toArray(new String[names.size()]);
  }
}
