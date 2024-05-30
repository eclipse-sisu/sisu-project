/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
package org.eclipse.sisu.space;

import java.net.URL;
import java.util.Enumeration;

/**
 * Finds (and optionally filters) {@link Class} resources from {@link ClassSpace}s.
 */
public interface ClassFinder
{
    /**
     * Searches the given {@link ClassSpace} for {@link Class} resources.
     * 
     * @param space The space to search
     * @return Sequence of Class URLs
     */
    Enumeration<URL> findClasses( ClassSpace space );
}
