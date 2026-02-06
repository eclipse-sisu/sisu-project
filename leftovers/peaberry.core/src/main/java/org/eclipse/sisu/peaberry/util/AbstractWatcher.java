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

package org.eclipse.sisu.peaberry.util;

import java.util.Map;

import org.eclipse.sisu.peaberry.Export;
import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceWatcher;

/**
 * Skeletal implementation to simplify development of {@link ServiceWatcher}s.
 * <p>
 * Developers only have to extend this class and provide implementations of the
 * {@link #adding(Import)}, {@link #modified(Object, Map)}, and {@link #removed}
 * service tracking methods. The design of this helper class is loosely based on
 * the OSGi ServiceTrackerCustomizer.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 * 
 * @since 1.1
 */
public abstract class AbstractWatcher<S>
    implements ServiceWatcher<S> {

  @SuppressWarnings("unchecked")
  public final <T extends S> Export<T> add(final Import<T> service) {
    final TrackingExport export = new TrackingExport((Import) service);
    return null == export.tracker ? null : (Export) export;
  }

  private final class TrackingExport
      extends SimpleExport<S> {

    S tracker; // customized tracker object

    TrackingExport(final Import<S> service) {
      super(service);

      tracker = adding(this);
    }

    @Override
    public synchronized void put(final S newInstance) {
      if (null != tracker) {
        removed(tracker);
        tracker = null;
      }

      super.put(newInstance);

      if (null != newInstance) {
        tracker = adding(this);
      }
    }

    @Override
    public synchronized void attributes(final Map<String, ?> newAttributes) {
      super.attributes(newAttributes);

      if (null != tracker) {
        modified(tracker, newAttributes);
      }
    }
  }

  /**
   * Notification that a service has been added to this watcher.
   * 
   * @param service new service handle
   * @return tracking instance, null if the service shouldn't be tracked
   */
  protected S adding(final Import<S> service) {
    return service.get();
  }

  /**
   * Notification that some service attributes have been modified.
   * 
   * @param instance tracking instance
   * @param attributes service attributes
   */
  protected void modified(final S instance, final Map<String, ?> attributes) {} // NOSONAR

  /**
   * Notification that a service has been removed from this watcher.
   * 
   * @param instance tracking instance
   */
  protected void removed(final S instance) {} // NOSONAR
}
