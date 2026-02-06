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

package org.eclipse.sisu.peaberry.internal;

import static java.security.AccessController.doPrivileged;
import static jsr166y.ConcurrentReferenceHashMap.ReferenceType.WEAK;
import static org.eclipse.sisu.peaberry.internal.ComputedMapFactory.computedMap;
import static org.eclipse.sisu.peaberry.internal.ImportGlue.generateProxy;
import static org.eclipse.sisu.peaberry.internal.ImportGlue.getClazzName;
import static org.eclipse.sisu.peaberry.internal.ImportGlue.getProxyName;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.ServiceException;
import org.eclipse.sisu.peaberry.ServiceUnavailableException;
import org.eclipse.sisu.peaberry.internal.ComputedMapFactory.Function;

/**
 * Custom classloader that provides optimized proxies for imported services.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ImportProxyClassLoader
    extends ClassLoader {

  private static final String UNAVAILABLE_CLAZZ_NAME = ServiceUnavailableException.class.getName();
  private static final String IMPORT_CLAZZ_NAME = Import.class.getName();

  private static final Object NULL_CLASS_LOADER_KEY = new Object();

  @SuppressWarnings("unchecked")
  static <T> Constructor<T> getProxyConstructor(final Class<T> clazz) {
    try {
      // use a different custom classloader for each class-space, to avoid leaks
      return (Constructor<T>) getProxyClass(clazz).getConstructor(Import.class);
    } catch (final LinkageError e) {
      throw new ServiceException(e);
    } catch (final NoSuchMethodException e) {
      // /CLOVER:OFF
      throw new ServiceException(e);
    } catch (final ClassNotFoundException e) {
      throw new ServiceException(e);
      // /CLOVER:ON
    }
  }

  /**
   * @return unique proxy class per given type
   */
  static Class<?> getProxyClass(final Class<?> clazz)
      throws ClassNotFoundException {

    final Object key = getKeyFromClassLoader(clazz.getClassLoader());
    final String name = getProxyName(clazz.getName());

    return LOADER_MAP.get(key).loadClass(name);
  }

  /**
   * @return non-null key for the given class loader
   */
  static Object getKeyFromClassLoader(final ClassLoader classLoader) {
    if (null != classLoader) {
      return classLoader;
    }

    try {
      return doPrivileged(new PrivilegedAction<>() {
        public ClassLoader run() {
          return getSystemClassLoader();
        }
      });
    } catch (final SecurityException e) {
      return NULL_CLASS_LOADER_KEY; // unable to canonicalise!
    }
  }

  /**
   * @return class loader related to the given key
   */
  static ClassLoader getClassLoaderFromKey(final Object key) {
    return NULL_CLASS_LOADER_KEY != key ? (ClassLoader) key : null; // NOSONAR
  }

  // weak map of classloaders, to allow eager collection of proxied classes
  private static final ConcurrentMap<Object, ClassLoader> LOADER_MAP =
      computedMap(WEAK, WEAK, 32, new Function<Object, ClassLoader>() {
        public ClassLoader compute(final Object parentKey) {
          return doPrivileged(new PrivilegedAction<>() {
            public ClassLoader run() {
              return new ImportProxyClassLoader(getClassLoaderFromKey(parentKey));
            }
          });
        }
      });

  // delegate to the original type's classloader
  ImportProxyClassLoader(final ClassLoader parent) {
    super(parent);
  }

  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {

    // short-circuit access to these classes
    if (IMPORT_CLAZZ_NAME.equals(name)) {
      return Import.class;
    }
    if (UNAVAILABLE_CLAZZ_NAME.equals(name)) {
      return ServiceUnavailableException.class;
    }

    return super.loadClass(name, resolve);
  }

  @Override
  protected Class<?> findClass(final String clazzOrProxyName)
      throws ClassNotFoundException {

    final String clazzName = getClazzName(clazzOrProxyName);

    // is this a new proxy class request?
    if (!clazzName.equals(clazzOrProxyName)) {
      final byte[] code = generateProxy(loadClass(clazzName));
      return defineClass(clazzOrProxyName, code, 0, code.length);
    }

    // ignore any non-proxy requests
    throw new ClassNotFoundException(clazzOrProxyName);
  }
}
