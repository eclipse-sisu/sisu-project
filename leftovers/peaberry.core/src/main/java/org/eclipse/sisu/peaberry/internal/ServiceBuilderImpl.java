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

import java.util.Map;

import org.eclipse.sisu.peaberry.AttributeFilter;
import org.eclipse.sisu.peaberry.ServiceRegistry;
import org.eclipse.sisu.peaberry.ServiceWatcher;
import org.eclipse.sisu.peaberry.builders.DecoratedServiceBuilder;
import org.eclipse.sisu.peaberry.builders.ExportProvider;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;
import org.eclipse.sisu.peaberry.builders.ProxyProvider;

import com.google.inject.Key;

/**
 * Default {@link DecoratedServiceBuilder} implementation.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ServiceBuilderImpl<T>
    implements DecoratedServiceBuilder<T> {

  // current builder state (can be cloned)
  private final ServiceSettings<T> settings;

  public ServiceBuilderImpl(final Key<T> key) {
    settings = new ServiceSettings<T>(key);
  }

  public ServiceBuilderImpl(final T instance) {
    settings = new ServiceSettings<T>(instance);
  }

  public ServiceBuilderImpl<T> decoratedWith(final Key<? extends ImportDecorator<? super T>> key) {
    settings.setDecorator(Setting.newSetting(key));
    return this;
  }

  public ServiceBuilderImpl<T> decoratedWith(final ImportDecorator<? super T> instance) {
    settings.setDecorator(Setting.<ImportDecorator<? super T>> newSetting(instance));
    return this;
  }

  public ServiceBuilderImpl<T> attributes(final Key<? extends Map<String, ?>> key) {
    settings.setAttributes(Setting.newSetting(key));
    return this;
  }

  public ServiceBuilderImpl<T> attributes(final Map<String, ?> instance) {
    settings.setAttributes(Setting.<Map<String, ?>> newSetting(instance));
    return this;
  }

  public ServiceBuilderImpl<T> filter(final Key<? extends AttributeFilter> key) {
    settings.setFilter(Setting.newSetting(key));
    return this;
  }

  public ServiceBuilderImpl<T> filter(final AttributeFilter instance) {
    settings.setFilter(Setting.newSetting(instance));
    return this;
  }

  public ServiceBuilderImpl<T> in(final Key<? extends ServiceRegistry> key) {
    settings.setRegistry(Setting.newSetting(key));
    return this;
  }

  public ServiceBuilderImpl<T> in(final ServiceRegistry instance) {
    settings.setRegistry(Setting.newSetting(instance));
    return this;
  }

  public ServiceBuilderImpl<T> out(final Key<? extends ServiceWatcher<? super T>> key) {
    settings.setWatcher(Setting.newSetting(key));
    return this;
  }

  public ServiceBuilderImpl<T> out(final ServiceWatcher<? super T> instance) {
    settings.setWatcher(Setting.<ServiceWatcher<? super T>> newSetting(instance));
    return this;
  }

  public ProxyProvider<T> single() {
    return new SingleServiceProvider<T>(settings);
  }

  public ProxyProvider<Iterable<T>> multiple() {
    return new MultipleServiceProvider<T>(settings);
  }

  public ExportProvider<T> export() {
    return new ExportedServiceProvider<T>(settings);
  }
}
