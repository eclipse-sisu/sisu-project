/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
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
