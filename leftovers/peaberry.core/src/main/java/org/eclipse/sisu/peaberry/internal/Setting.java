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

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Immutable setting that accepts either a binding key or explicit instance.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
@SuppressWarnings("PMD.AbstractNaming")
abstract class Setting<T> {

  /**
   * @param injector optional injector
   * @return injected setting value
   */
  abstract T get(final Injector injector);

  /**
   * @return setting based on explicit instance
   */
  static <T> Setting<T> newSetting(final T instance) {
    if (null == instance) {
      // null instances are not tolerated
      throw new IllegalArgumentException("null instance");
    }

    return new Setting<T>() {
      private boolean configured;

      @Override
      synchronized T get(final Injector injector) {
        if (!configured && null != injector) {
          // given value may need injecting
          injector.injectMembers(instance);
          configured = true;
        }
        return instance;
      }
    };
  }

  /**
   * @return setting based on binding key
   */
  static <T> Setting<T> newSetting(final Key<? extends T> key) {
    if (null == key) {
      // null binding keys are not tolerated
      throw new IllegalArgumentException("null binding key");
    }

    return new Setting<T>() {
      private T instance;

      @Override
      synchronized T get(final Injector injector) {
        if (null == instance) {
          if (null == injector) {
            throw new IllegalArgumentException("missing injector for setting: " + key);
          }
          // query the injector for the value
          instance = injector.getInstance(key);
        }
        return instance;
      }
    };
  }

  @SuppressWarnings("unchecked")
  static <T> Setting<T> nullSetting() {
    return (Setting<T>) NULL_SETTING;
  }

  // constant null setting, safe to share between builders
  private static final Setting<Object> NULL_SETTING = new Setting<>() {
    @Override
    Object get(final Injector injector) {
      return null;
    }
  };
}
