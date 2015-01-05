/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
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
