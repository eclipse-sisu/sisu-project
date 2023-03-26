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

/**
 * Source of Plexus component beans and associated metadata.
 */
public interface PlexusBeanSource
{
    /**
     * Returns metadata associated with the given Plexus bean implementation.
     * 
     * @param implementation The bean implementation
     * @return Metadata associated with the given bean
     */
    PlexusBeanMetadata getBeanMetadata( Class<?> implementation );
}
