/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * SPI for contributing {@link com.google.inject.Binding}s to the {@link org.eclipse.sisu.locators.MutableBeanLocator}.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.locators.spi.BindingDistributor}
 * <dd>Distributor of {@link com.google.inject.Binding}s retrieved from a series of {@link org.eclipse.sisu.locators.spi.BindingPublisher}s.
 * <dt>{@link org.eclipse.sisu.locators.spi.BindingPublisher}
 * <dd>Publisher of {@link com.google.inject.Binding}s to interested {@link org.eclipse.sisu.locators.spi.BindingSubscriber}s.
 * <dt>{@link org.eclipse.sisu.locators.spi.BindingSubscriber}
 * <dd>Subscriber of {@link com.google.inject.Binding}s from one or more {@link org.eclipse.sisu.locators.spi.BindingPublisher}s.
 * </dl>
 */
package org.eclipse.sisu.locators.spi;

