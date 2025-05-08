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
