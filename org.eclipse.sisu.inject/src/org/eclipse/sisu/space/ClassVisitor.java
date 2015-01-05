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

import java.lang.reflect.Modifier;

/**
 * Something that can visit class definitions.
 */
public interface ClassVisitor
{
    /**
     * Non-instantiable classes: INTERFACE | ABSTRACT | ANNOTATION | ENUM | SYNTHETIC.
     */
    int NON_INSTANTIABLE = Modifier.INTERFACE | Modifier.ABSTRACT | 0x00007000;

    /**
     * Enters the class definition.
     * 
     * @param modifiers The access modifiers
     * @param name The internal name, such as "javax/inject/Provider"
     * @param _extends Extends this superclass
     * @param _implements Implements these interfaces
     */
    void enterClass( int modifiers, String name, String _extends, String[] _implements );

    /**
     * Visits an annotation declared on the class.
     * 
     * @param desc The JVM descriptor for the annotation class, such as "Ljavax/inject/Qualifier;"
     * @return Annotation visitor; {@code null} if it is not interested in visiting the annotation
     * @see SpaceScanner#jvmDescriptor(Class)
     */
    AnnotationVisitor visitAnnotation( String desc );

    /**
     * Leaves the class definition.
     */
    void leaveClass();
}
