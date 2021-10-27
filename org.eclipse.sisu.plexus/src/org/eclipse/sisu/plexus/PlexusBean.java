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

import java.util.Map.Entry;

/**
 * Plexus bean mapping; from hint to instance.
 */
public interface PlexusBean<T>
    extends Entry<String, T>
{
    /**
     * @return Human readable description
     */
    String getDescription();

    /**
     * @return Bean implementation class
     */
    Class<T> getImplementationClass();
}
