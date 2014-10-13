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

package org.eclipse.sisu.peaberry.util.decorators;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceException;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;
import org.eclipse.sisu.peaberry.util.DelegatingImport;

/**
 * An {@link ImportDecorator} that caches the first valid service instance and
 * uses that until it becomes invalid. The decorator then calls the reset task
 * to see if it should reset the cache and get a new service instance.
 * <p>
 * If no reset task is provided, the service instance cache is never reset.
 * <p>
 * Note: sticky decorators only really make sense for <i>single</i> services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class StickyDecorator<S>
    implements ImportDecorator<S> {

  final Callable<Boolean> resetTask;

  public StickyDecorator(final Callable<Boolean> resetTask) {
    this.resetTask = resetTask;
  }

  private final class StickyImport<T>
      extends DelegatingImport<T> {

    private WeakReference<T> instanceRef;
    private boolean reset = true;

    StickyImport(final Import<T> service) {
      super(service);
    }

    @Override
    public synchronized T get() {

      // when service becomes unavailable we ask if the client wants to reset
      if (null != resetTask && null != instanceRef && !super.available()) {

        // always clear the current service once it's invalid
        instanceRef.clear();
        instanceRef = null;

        try {
          // should we reset and take the next valid service?
          reset = resetTask.call();
        } catch (final Exception e) {
          throw new ServiceException("Exception in resetTask", e);
        }

        if (reset) {
          super.unget(); // balance previous successful get
        }
      }

      if (reset) {
        try {
          final T instance = super.get();
          instanceRef = new WeakReference<T>(instance);
          reset = null == instance;
        } finally {
          if (reset) {
            instanceRef = null;
            super.unget(); // balance previous unsuccessful get
          }
        }
      }

      return null == instanceRef ? null : instanceRef.get();
    }

    @Override
    public void unget() {/* nothing to do */}
  }

  public <T extends S> Import<T> decorate(final Import<T> service) {
    return new StickyImport<T>(service);
  }
}
