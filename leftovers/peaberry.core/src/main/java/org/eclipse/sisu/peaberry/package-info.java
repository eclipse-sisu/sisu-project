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

/**
 * <i>peaberry</i> - Dynamic services for <a target="_blank"
 * href="http://code.google.com/p/google-guice/">Google-Guice</a>.
 *
 * <p>The principal members of this package are:
 *
 * <dl>
 *
 * <dt>{@link org.eclipse.sisu.peaberry.Peaberry}
 * <dd>The builder that can assemble providers to import or export dynamic services.
 *
 * <dt>{@link org.eclipse.sisu.peaberry.ServiceUnavailableException}
 * <dd>The exception thrown when you attempt to use a service that is not available.
 *
 * <dt>{@link org.eclipse.sisu.peaberry.ServiceRegistry}
 * <dd>The interface you can implement in order to plug-in other service frameworks.
 *
 * <dt>{@link org.eclipse.sisu.peaberry.ServiceWatcher}
 * <dd>The interface you can implement in order to watch services coming and going.
 *
 * </dl>
 */
package org.eclipse.sisu.peaberry;

