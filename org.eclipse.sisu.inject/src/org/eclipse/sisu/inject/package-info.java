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
 * Custom bean injection.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.inject.BeanListener}
 * <dd>{@link com.google.inject.spi.TypeListener} that listens for bean types and wires up their properties.
 * <dt>{@link org.eclipse.sisu.inject.BeanBinder}
 * <dd>Provides custom {@link org.eclipse.sisu.inject.PropertyBinder}s for bean types.
 * <dt>{@link org.eclipse.sisu.inject.PropertyBinder}
 * <dd>Provides custom {@link org.eclipse.sisu.inject.PropertyBinding}s for bean properties.
 * <dt>{@link org.eclipse.sisu.inject.PropertyBinding}
 * <dd>Injects a customized bean property into bean instances.
 * </dl>
 */
package org.eclipse.sisu.inject;

