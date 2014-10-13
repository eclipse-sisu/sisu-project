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

package org.eclipse.sisu.peaberry.eclipse.internal;

import org.eclipse.sisu.peaberry.eclipse.EclipseRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi {@link BundleActivator} that cleans up and removes registry listeners.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class Activator
    implements BundleActivator {

  public void start(final BundleContext bundleContext) {/* nothing to do */}

  public void stop(final BundleContext bundleContext) {
    ((EclipseRegistry) EclipseRegistry.eclipseRegistry()).shutdown();
  }
}
