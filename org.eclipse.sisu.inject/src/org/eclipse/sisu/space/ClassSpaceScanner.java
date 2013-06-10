/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.asm.ClassReader;
import org.eclipse.sisu.space.asm.Opcodes;
import org.eclipse.sisu.space.asm.Type;

/**
 * Makes a {@link ClassSpaceVisitor} visit a {@link ClassSpace}; can be directed by an optional {@link ClassFinder}.
 */
public final class ClassSpaceScanner
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int ASM_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassFinder finder;

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ClassSpaceScanner( final ClassFinder finder, final ClassSpace space )
    {
        this.finder = finder;
        this.space = space;
    }

    public ClassSpaceScanner( final ClassSpace space )
    {
        this( new DefaultClassFinder(), space );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Makes the given {@link ClassSpaceVisitor} visit the {@link ClassSpace} of this scanner.
     * 
     * @param visitor The class space visitor
     */
    public void accept( final ClassSpaceVisitor visitor )
    {
        visitor.enter( space );

        for ( final Enumeration<URL> result = finder.findClasses( space ); result.hasMoreElements(); )
        {
            final URL url = result.nextElement();
            final ClassVisitor cv = visitor.visitClass( url );
            if ( null != cv )
            {
                accept( cv, url );
            }
        }

        visitor.leave();
    }

    /**
     * Makes the given {@link ClassVisitor} visit the class contained in the resource {@link URL}.
     * 
     * @param visitor The class space visitor
     * @param url The class resource URL
     */
    public static void accept( final ClassVisitor visitor, final URL url )
    {
        if ( null == url )
        {
            return; // nothing to visit
        }
        try
        {
            final InputStream in = url.openStream();
            try
            {
                new ClassReader( in ).accept( adapt( visitor ), ASM_FLAGS );
            }
            finally
            {
                in.close();
            }
        }
        catch ( final ArrayIndexOutOfBoundsException e ) // NOPMD
        {
            // ignore broken class constant pool in icu4j
        }
        catch ( final Exception e )
        {
            Logs.trace( "Problem scanning: {}", url, e );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adapts the given {@link ClassVisitor} to its equivalent ASM form.
     * 
     * @param _cv The class visitor to adapt
     * @return ASM-compatible class visitor
     */
    private static org.eclipse.sisu.space.asm.ClassVisitor adapt( final ClassVisitor _cv )
    {
        return new org.eclipse.sisu.space.asm.ClassVisitor( Opcodes.ASM4 )
        {
            @Override
            public void visit( final int version, final int access, final String name, final String signature,
                               final String superName, final String[] interfaces )
            {
                _cv.enter( access, name, superName, interfaces );
            }

            @Override
            public org.eclipse.sisu.space.asm.AnnotationVisitor visitAnnotation( final String desc,
                                                                                 final boolean visible )
            {
                final AnnotationVisitor _av = _cv.visitAnnotation( desc );
                if ( null == _av )
                {
                    return null;
                }
                _av.enter();
                return new org.eclipse.sisu.space.asm.AnnotationVisitor( Opcodes.ASM4 )
                {
                    @Override
                    public void visit( final String name, final Object value )
                    {
                        _av.visitElement( name, value instanceof Type ? ( (Type) value ).getClassName() : value );
                    }

                    @Override
                    public void visitEnd()
                    {
                        _av.leave();
                    }
                };
            }

            @Override
            public void visitEnd()
            {
                _cv.leave();
            }
        };
    }
}
