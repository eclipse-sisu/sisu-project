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

package org.eclipse.sisu.peaberry;

import org.eclipse.sisu.peaberry.builders.DecoratedServiceBuilder;
import org.eclipse.sisu.peaberry.internal.ServiceBuilderImpl;
import org.eclipse.sisu.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * Guice extension that supports injection and outjection of dynamic services.
 * <p>
 * For example, injecting a stock quote service:
 * 
 * <pre> {@literal @}Inject
 * StockQuote quote;
 * ...
 * bind(StockQuote.class).toProvider(service(StockQuote.class).single());</pre>
 * 
 * Injecting many stock quote services:
 * 
 * <pre> {@literal @}Inject
 * Iterable&lt;StockQuote&gt; quotes;
 * ...
 * bind(iterable(StockQuote.class)).toProvider(service(StockQuote.class).multiple());</pre>
 * 
 * Exporting an implementation as a stock quote service:
 * 
 * <pre> {@literal @}Inject
 * // the service can be controlled by the Export handle
 * Export&lt;StockQuote&gt; exportedQuote;
 * ...
 * // the service is exported at injection time
 * bind(export(StockQuote.class)).toProvider(service(myQuoteImpl).export());</pre>
 * 
 * Applying a custom filter to find a specific service:
 * 
 * <pre> service(StockQuote.class).filter(ldap(&quot;(Currency=GBP)&quot;)).single()</pre>
 * 
 * Applying custom attributes to an exported service:
 * 
 * <pre> service(myQuoteImpl).attributes(names(&quot;Currency=GBP&quot;)).export()</pre>
 * 
 * (the ldap and names utility methods are from {@link org.eclipse.sisu.peaberry.util})
 * <p>
 * You can also decorate services with additional behaviour:
 * 
 * <pre> service(StockQuote.class).decoratedWith(someDecoratorImpl).single()</pre>
 * 
 * or ask for them to be injected directly, instead of using a dynamic proxy:
 * 
 * <pre> service(StockQuote.class).single().direct()</pre>
 * 
 * similarly, if you don't want to bother with an Export handle when exporting:
 * 
 * <pre> service(myQuoteImpl).export().direct()</pre>
 * 
 * Outjection is a way of actively monitoring for services instead of polling:
 * 
 * <pre> service(StockQuote.class).out(myWatcherImpl).multiple()</pre>
 * 
 * the given watcher is notified when matching services appear and disappear. <br>
 * ({@link org.eclipse.sisu.peaberry.util} has abstract watcher and decorator classes)
 * <p>
 * See the online <a href="http://code.google.com/p/peaberry/wiki/UserGuide"
 * target="_blank">User Guide</a> for the latest examples and suggested best
 * practice.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class Peaberry {

  // instances not allowed
  private Peaberry() {}

  /**
   * Start building a dynamic service provider for the given key.
   * 
   * @param key binding key
   * @return dynamic service builder
   */
  public static <T> DecoratedServiceBuilder<T> service(final Key<T> key) {
    return new ServiceBuilderImpl<T>(key);
  }

  /**
   * Start building a dynamic service provider for the given type.
   * 
   * @param type binding type
   * @return dynamic service builder
   */
  public static <T> DecoratedServiceBuilder<T> service(final TypeLiteral<T> type) {
    return new ServiceBuilderImpl<T>(Key.get(type));
  }

  /**
   * Start building a dynamic service provider for the given class.
   * 
   * @param clazz binding class
   * @return dynamic service builder
   */
  public static <T> DecoratedServiceBuilder<T> service(final Class<T> clazz) {
    return new ServiceBuilderImpl<T>(Key.get(clazz));
  }

  /**
   * Start building a dynamic service provider for the given instance.
   * 
   * @param instance service instance
   * @return dynamic service builder
   */
  public static <T> DecoratedServiceBuilder<T> service(final T instance) {
    return new ServiceBuilderImpl<T>(instance);
  }

  /**
   * Name of system property ({@code "org.eclipse.sisu.peaberry.cache.interval"})
   * suggests the time in milliseconds between flushes of the service cache.
   */
  public static final String CACHE_INTERVAL_HINT = "org.eclipse.sisu.peaberry.cache.interval";

  /**
   * Name of system property ({@code "org.eclipse.sisu.peaberry.cache.generations"})
   * suggests the number of flushes before an unused service is released.
   */
  public static final String CACHE_GENERATIONS_HINT = "org.eclipse.sisu.peaberry.cache.generations";

  /**
   * Name of system property ({@code "org.eclipse.sisu.peaberry.filter.native"})
   * suggests using {@code AttributeFilter.toString()} as native filter.
   */
  public static final String NATIVE_FILTER_HINT = "org.eclipse.sisu.peaberry.filter.native";

  /**
   * Create a new Guice binding {@link Module} for the given bundle context.
   * 
   * @param bundleContext current bundle context
   * @return OSGi specific Guice bindings
   */
  public static Module osgiModule(final BundleContext bundleContext) {
    return new OSGiModule(bundleContext);
  }

  /**
   * Create a new Guice binding {@link Module} for the given bundle context
   * along with a list of additional {@link ServiceRegistry}s to query when
   * injecting or watching for services.
   * 
   * @param bundleContext current bundle context
   * @param registries extra service registries
   * @return OSGi specific Guice bindings
   * 
   * @since 1.1
   */
  public static Module osgiModule(final BundleContext bundleContext,
      final ServiceRegistry... registries) {
    return new OSGiModule(bundleContext, registries);
  }

  /**
   * Create a new Guice binding {@link Module} that uses an externally bound
   * bundle context along with a list of additional {@link ServiceRegistry}s
   * to query when injecting or watching for services.
   * 
   * @param registries extra service registries
   * @return OSGi specific Guice bindings
   * 
   * @since 1.3
   */
  public static Module osgiModule(final ServiceRegistry... registries) {
    return new OSGiModule(registries);
  }
}
