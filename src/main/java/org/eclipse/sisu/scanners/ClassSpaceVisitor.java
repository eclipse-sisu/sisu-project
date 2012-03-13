/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.scanners;

import java.net.URL;

import org.eclipse.sisu.reflect.ClassSpace;
import org.objectweb.asm.ClassVisitor;

/**
 * ASM-style visitor that visits a {@link ClassSpace}. The methods of this interface must be called in the following
 * order: {@code visit ( visitClass )* visitEnd}.
 */
public interface ClassSpaceVisitor
{
    /**
     * Visits the start of the class space.
     * 
     * @param space The class space
     */
    void visit( final ClassSpace space );

    /**
     * Visits a class resource in the class space.
     * 
     * @param url The class resource URL
     * @return Class visitor; {@code null} if this visitor is not interested in visiting the class
     */
    ClassVisitor visitClass( final URL url );

    /**
     * Visits the end of the class space.
     */
    void visitEnd();
}
