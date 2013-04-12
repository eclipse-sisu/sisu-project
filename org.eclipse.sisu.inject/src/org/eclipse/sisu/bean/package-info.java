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
 * Custom bean injection.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link BeanListener}
 * <dd>{@link com.google.inject.spi.TypeListener} that listens for bean types and wires up their properties.
 * <dt>{@link BeanBinder}
 * <dd>Provides custom {@link PropertyBinder}s for bean types.
 * <dt>{@link PropertyBinder}
 * <dd>Provides custom {@link PropertyBinding}s for bean properties.
 * <dt>{@link PropertyBinding}
 * <dd>Injects a customized bean property into bean instances.
 * <dt>{@link BeanProperties}
 * <dd>Picks out potential bean properties from declared class members.
 * </dl>
 */
package org.eclipse.sisu.bean;

