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
 * Locate qualified bean implementations across multiple injectors.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.locators.BeanLocator}
 * <dd>Finds and tracks bean implementations annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.eclipse.sisu.locators.MutableBeanLocator}
 * <dd>Mutable {@link org.eclipse.sisu.locators.BeanLocator} that distributes bindings from zero or more {@link org.eclipse.sisu.locators.spi.BindingPublisher}s.
 * <dt>{@link org.eclipse.sisu.locators.BeanDescription}
 * <dd>Source location mixin used to supply descriptions to the {@link org.eclipse.sisu.locators.BeanLocator}.
 * <dt>{@link org.eclipse.sisu.locators.HiddenBinding}
 * <dd>Source location mixin used to hide bindings from the {@link org.eclipse.sisu.locators.BeanLocator}.
 * </dl>
 */
package org.eclipse.sisu.locators;

