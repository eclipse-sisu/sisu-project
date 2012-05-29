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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Skeletal {@link ClassVisitor} that helps minimize the effort required to implement bytecode scanners.
 */
public class EmptyClassVisitor
    implements ClassVisitor
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
    }

    public void visitSource( final String source, final String debug )
    {
    }

    public void visitOuterClass( final String owner, final String name, final String desc )
    {
    }

    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        return null;
    }

    public void visitAttribute( final Attribute attr )
    {
    }

    public void visitInnerClass( final String name, final String outerName, final String innerName, final int access )
    {
    }

    public FieldVisitor visitField( final int access, final String name, final String desc, final String signature,
                                    final Object value )
    {
        return null;
    }

    public MethodVisitor visitMethod( final int access, final String name, final String desc, final String signature,
                                      final String[] exceptions )
    {
        return null;
    }

    public void visitEnd()
    {
    }
}
