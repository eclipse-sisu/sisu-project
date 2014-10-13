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

import static java.util.Collections.singletonMap;

import org.eclipse.sisu.peaberry.ServiceException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Custom {@link Scope} that provides singletons per {@link BundleContext}.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class BundleScopeImpl
    implements Scope {

  // attribute used to select bundle-scoped services
  private static final String BUNDLE_ID = "bundle.id";

  final Provider<BundleContext> contextProvider;

  BundleScopeImpl(final Provider<BundleContext> contextProvider) {
    this.contextProvider = contextProvider;
  }

  public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
    final String clazzName = key.getTypeLiteral().getRawType().getName();

    return new Provider<T>() {

      private volatile T instance; // cache for repeated requests

      @SuppressWarnings("unchecked")
      public T get() {

        if (null == instance) {
          final BundleContext bundleContext = contextProvider.get();
          synchronized (bundleContext) {
            if (null == instance) {
              final ServiceReference[] refs;

              // filter services to those registered by this context
              final long bundleId = bundleContext.getBundle().getBundleId();
              final String filter = '(' + BUNDLE_ID + '=' + bundleId + ')';

              try {
                // see if the context has an instance for this binding class
                refs = bundleContext.getServiceReferences(clazzName, filter);
              } catch (final InvalidSyntaxException e) {
                throw new ServiceException(e); // this should never happen!
              }

              if (null != refs && refs.length > 0) {
                // retrieve the existing instance from the registry
                instance = (T) bundleContext.getService(refs[0]);
              } else {
                instance = creator.get();

                // register the brand-new instance with the registry
                bundleContext.registerService(clazzName, instance,
                    new AttributeDictionary(singletonMap(BUNDLE_ID, bundleId)));
              }
            }
          }
        }

        return instance;
      }

      @Override
      public String toString() {
        return String.format("%s[BundleScope [%s]]", creator, contextProvider.get().getBundle());
      }
    };
  }

  @Override
  public String toString() {
    return "BundleScope";
  }
}
