/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
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
 * <dt>{@link BeanLocator}
 * <dd>Finds and tracks bean implementations annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link MutableBeanLocator}
 * <dd>Mutable {@link BeanLocator} that distributes bindings from zero or more {@link BindingPublisher}s.
 * <dt>{@link DescribedBinding}
 * <dd>Source location mixin used to supply descriptions to the {@link BeanLocator}.
 * <dt>{@link HiddenBinding}
 * <dd>Source location mixin used to hide bindings from the {@link BeanLocator}.
 * <dt>{@link BindingPublisher}
 * <dd>Publisher of {@link com.google.inject.Binding}s to interested {@link BindingSubscriber}s.
 * <dt>{@link BindingSubscriber}
 * <dd>Subscriber of {@link com.google.inject.Binding}s from one or more {@link BindingPublisher}s.
 * <dt>{@link Logs}
 * <dd>Utility methods for dealing with container logging and recovery.
 * <dt>{@link Soft}
 * <dd>Utility methods for dealing with {@link SoftReference} collections.
 * <dt>{@link Weak}
 * <dd>Utility methods for dealing with {@link WeakReference} collections.
 * <dt>{@link TypeArguments}
 * <dd>Utility methods for dealing with generic type arguments.
 * </dl>
 */
package org.eclipse.sisu.inject;

