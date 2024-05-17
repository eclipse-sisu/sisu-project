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

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperty;

/**
 * Supplies metadata associated with a particular Plexus bean implementation.
 */
public interface PlexusBeanMetadata
{
    /**
     * @return {@code true} if there is no more metadata; otherwise {@code false}
     */
    boolean isEmpty();

    /**
     * Returns @{@link Configuration} metadata for the given property of the Plexus bean.
     * 
     * @param property The bean property
     * @return Configuration metadata; {@code null} if no such metadata is available
     */
    Configuration getConfiguration( BeanProperty<?> property );

    /**
     * Returns @{@link Requirement} metadata for the given property of the Plexus bean.
     * 
     * @param property The bean property
     * @return Requirement metadata; {@code null} if no such metadata is available
     */
    Requirement getRequirement( BeanProperty<?> property );
}
