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
 * Automatic bean binding.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link WireModule}
 * <dd>Adds {@link org.eclipse.sisu.inject.BeanLocator} bindings for any non-local bean dependencies.
 * <dt>{@link FileTypeConverter}
 * <dd>{@link com.google.inject.spi.TypeConverter} {@link com.google.inject.Module} that converts constants to {@link java.io.File}s.
 * <dt>{@link URLTypeConverter}
 * <dd>{@link com.google.inject.spi.TypeConverter} {@link com.google.inject.Module} that converts constants to {@link java.net.URL}s.
 * </dl>
 */
package org.eclipse.sisu.wire;

