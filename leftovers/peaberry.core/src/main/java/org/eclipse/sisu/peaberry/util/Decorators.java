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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;
import org.eclipse.sisu.peaberry.util.decorators.DecoratorChain;
import org.eclipse.sisu.peaberry.util.decorators.InterceptingDecorator;
import org.eclipse.sisu.peaberry.util.decorators.StickyDecorator;

import com.google.inject.matcher.Matcher;

/**
 * Methods for creating various general purpose {@link ImportDecorator}s.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class Decorators {

  // instances not allowed
  private Decorators() {}

  /**
   * An {@link ImportDecorator} that caches the first valid service instance and
   * uses that until it becomes invalid. The decorator then calls the reset task
   * to see if it should reset the cache and get a new service instance.
   * <p>
   * If no reset task is provided, the service instance cache is never reset.
   * <p>
   * Note: sticky decorators only really make sense for <i>single</i> services.
   * 
   * @param resetTask called when cached service becomes invalid, may be null
   * @return decorator that caches service instances
   */
  public static <S> ImportDecorator<S> sticky(final Callable<Boolean> resetTask) {
    return new StickyDecorator<S>(resetTask);
  }

  /**
   * An {@link ImportDecorator} that applies decorators in a chain, right-left.
   * 
   * @param decorators sequence of decorators
   * @return decorator combining the given decorators
   */
  @SuppressWarnings("unchecked")
  public static <S> ImportDecorator<S> chain(final ImportDecorator... decorators) {
    return new DecoratorChain<S>(decorators);
  }

  /**
   * An {@link ImportDecorator} that supports {@link MethodInterceptor}s.
   * 
   * @param classMatcher matches interfaces to be intercepted
   * @param methodMatcher matches methods to be intercepted
   * @param interceptors sequence of method interceptors
   * @return decorator that intercepts imported services
   */
  public static <S> ImportDecorator<S> intercept(final Matcher<? super Class<?>> classMatcher,
      final Matcher<? super Method> methodMatcher, final MethodInterceptor... interceptors) {
    return new InterceptingDecorator<S>(classMatcher, methodMatcher, interceptors);
  }
}
