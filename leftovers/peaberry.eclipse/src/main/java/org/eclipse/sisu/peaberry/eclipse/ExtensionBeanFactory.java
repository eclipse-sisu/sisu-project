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

package org.eclipse.sisu.peaberry.eclipse;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.sisu.peaberry.ServiceException;
import org.osgi.framework.Bundle;

/**
 * A factory that attempts to create bean instances from Eclipse Extensions.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
final class ExtensionBeanFactory {

  private static final String CONTENT_KEY = "text()";

  // instances not allowed
  private ExtensionBeanFactory() {}

  static Object newInstance(final Class<?> clazz, final IConfigurationElement config) {

    // client just wants the actual configuration object
    if (Object.class == clazz || IConfigurationElement.class == clazz) {
      return config;
    }

    try {
      // first try to instantiate direct class
      return newExtensionImpl(clazz, config);
    } catch (final RuntimeException re) {/* fall back to proxy */} // NOSONAR

    // create proxy based on the supplied interface
    final ClassLoader loader = clazz.getClassLoader();
    final Class<?>[] api = new Class[]{clazz};

    return newProxyInstance(loader, api, new ExtensionBeanHandler(config));
  }

  static Object newExtensionImpl(final Class<?> clazz, final IConfigurationElement config) {

    // assume name is kept under "class" (unless mapped)
    final String clazzKey = mapName(clazz, "class");
    final String clazzName = mapContent(config, clazzKey);

    // make sure implementation is compatible with the required interface
    if (!clazz.isAssignableFrom(loadExtensionClass(config, clazzName))) {
      throw new ClassCastException(clazz + " is not assignable from: " + clazzName);
    }

    try {
      return config.createExecutableExtension(clazzKey);
    } catch (final CoreException e) {
      throw new ServiceException(e);
    }
  }

  static String mapName(final AnnotatedElement type, final String name) {
    if (null != findAnnotation(type, "MapContent")) {
      return CONTENT_KEY;
    }
    final String mapName = getAnnotationValue(findAnnotation(type, "MapName"), String.class);
    return null == mapName || mapName.length() == 0 ? name : mapName;
  }

  static String mapContent(final IConfigurationElement config, final String elementKey) {
    return CONTENT_KEY.equals(elementKey) ? config.getValue() : config.getAttribute(elementKey);
  }

  static Annotation findAnnotation(final AnnotatedElement element, final String simpleName) {
    for (final Annotation a : element.getAnnotations()) {
      if (simpleName.equals(a.annotationType().getSimpleName())) {
        return a;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  static <T> T getAnnotationValue(final Annotation element, final Class<? extends T> clazz) {

    try {
      final Method m = element.annotationType().getDeclaredMethod("value");
      if (clazz.isAssignableFrom(m.getReturnType())) {
        return (T) m.invoke(element);
      }
    } catch (final Exception e) {/* no such setting */} // NOSONAR

    return null;
  }

  static Class<?> loadExtensionClass(final IConfigurationElement config, final String clazzName) {
    final Bundle bundle = ContributorFactoryOSGi.resolve(config.getContributor());

    String value = clazzName;
    int n = value.indexOf(':');

    // unravel the factory indirection to get the real class
    if (value.startsWith(GuiceExtensionFactory.class.getName())) {
      value = n < 0 ? config.getAttribute("id") : value.substring(n + 1);
      n = value.indexOf(':');
    }

    try {
      return bundle.loadClass(n < 0 ? value : value.substring(0, n));
    } catch (final ClassNotFoundException e) {
      throw new ServiceException(e);
    }
  }
}
