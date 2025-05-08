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
package org.eclipse.sisu.space;

import java.net.URL;

/**
 * Something that can visit {@link ClassSpace}s.
 */
public interface SpaceVisitor
{
    /**
     * Enters the class space.
     * 
     * @param space The class space
     */
    void enterSpace( ClassSpace space );

    /**
     * Visits a class resource in the class space.
     * 
     * @param url The class resource URL
     * @return Class visitor; {@code null} if it is not interested in visiting the class
     */
    ClassVisitor visitClass( URL url );

    /**
     * Leaves the class space.
     */
    void leaveSpace();
}
