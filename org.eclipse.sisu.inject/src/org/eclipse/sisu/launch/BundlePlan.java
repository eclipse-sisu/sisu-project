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
package org.eclipse.sisu.launch;

import org.eclipse.sisu.inject.BindingPublisher;
import org.osgi.framework.Bundle;

/**
 * Defines how to publish bundles containing specific types of components.
 */
public interface BundlePlan
{
    /**
     * Returns {@code true} if it applies to the bundle; otherwise {@code false}.
     */
    boolean appliesTo( Bundle bundle );

    /**
     * Produces a {@link BindingPublisher} of components from the given bundle.
     * 
     * @param bundle The bundle
     * @return Publisher of bindings
     */
    BindingPublisher publish( Bundle bundle );
}
