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
