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
 * Automatic bean binding.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.eclipse.sisu.binders.SpaceModule}
 * <dd>Scans a {@link org.eclipse.sisu.reflect.ClassSpace} for beans and adds any qualified bindings.
 * <dt>{@link org.eclipse.sisu.binders.WireModule}
 * <dd>Adds {@link org.eclipse.sisu.locators.BeanLocator} bindings for any non-local bean dependencies.
 * </dl>
 */
package org.eclipse.sisu.binders;

