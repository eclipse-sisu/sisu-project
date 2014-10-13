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

import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.Map;

import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.cache.AbstractServiceImport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * {@link Import} implementation backed by an OSGi {@link ServiceReference}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class OSGiServiceImport<T>
    extends AbstractServiceImport<T> {

  private final BundleContext bundleContext;
  private final ServiceReference ref;

  // heavily used attributes
  private final long id;
  private int rank;

  private final Map<String, ?> attributes;

  OSGiServiceImport(final BundleContext bundleContext, final ServiceReference ref) {
    this.bundleContext = bundleContext;
    this.ref = ref;

    // cache attributes used when sorting services
    id = getNumberProperty(SERVICE_ID).longValue();
    rank = getNumberProperty(SERVICE_RANKING).intValue();

    attributes = new OSGiServiceAttributes(ref);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T acquireService() {
    return (T) bundleContext.getService(ref);
  }

  public Map<String, ?> attributes() {
    return attributes;
  }

  @Override
  protected boolean hasRankingChanged() {
    // ranking is mutable...
    final int oldRank = rank;
    rank = getNumberProperty(SERVICE_RANKING).intValue();
    return oldRank != rank;
  }

  @Override
  protected void releaseService(final T o) {
    bundleContext.ungetService(ref);
  }

  @Override
  public boolean equals(final Object rhs) {
    if (rhs instanceof OSGiServiceImport<?>) {
      // service id is a unique identifier
      return id == ((OSGiServiceImport<?>) rhs).id;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ id >>> 32);
  }

  public int compareTo(final Import<T> rhs) {
    final OSGiServiceImport<T> rhsImport = (OSGiServiceImport<T>) rhs;

    if (id == rhsImport.id) {
      return 0;
    }

    if (rank == rhsImport.rank) {
      // prefer lower service id
      return id < rhsImport.id ? -1 : 1;
    }

    // but higher ranking beats all
    return rank > rhsImport.rank ? -1 : 1;
  }

  private Number getNumberProperty(final String key) {
    final Object num = ref.getProperty(key);
    return num instanceof Number ? (Number) num : 0;
  }
}
