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
package org.eclipse.sisu.plexus;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.space.QualifiedTypeListener;

/**
 * {@link QualifiedTypeListener} that also listens for Plexus components.
 */
public interface PlexusTypeListener
    extends QualifiedTypeListener
{
    /**
     * Invoked when the {@link PlexusTypeListener} finds a Plexus component.
     * 
     * @param component The Plexus component
     * @param implementation The implementation
     * @param source The source of this component
     */
    void hear( Component component, DeferredClass<?> implementation, Object source );
}
