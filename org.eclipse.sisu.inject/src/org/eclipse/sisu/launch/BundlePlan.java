/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
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
 * Something that can prepare {@link BindingPublisher}s for component bundles.
 */
public interface BundlePlan
{
    /**
     * Prepares a {@link BindingPublisher} of components for the given bundle.
     * 
     * @param bundle The bundle
     * @return Publisher of bindings; {@code null} if the plan doesn't apply
     */
    BindingPublisher prepare( Bundle bundle );
}
