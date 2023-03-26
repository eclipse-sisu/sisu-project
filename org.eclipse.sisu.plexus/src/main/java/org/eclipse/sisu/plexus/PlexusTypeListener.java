/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
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
