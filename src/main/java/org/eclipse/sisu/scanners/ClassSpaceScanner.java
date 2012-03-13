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

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.Logs;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * ASM-style scanner that makes a {@link ClassSpaceVisitor} visit an existing {@link ClassSpace}.
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

    public ClassSpaceScanner( final ClassSpace space )
    {
        this( null, space );
    }

    public ClassSpaceScanner( final ClassFinder finder, final ClassSpace space )
    {
        this.finder = finder;
        this.space = space;
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
        visitor.visit( space );

        final Enumeration<URL> result =
            null != finder ? finder.findClasses( space ) : space.findEntries( null, "*.class", true );

        while ( result.hasMoreElements() )
        {
            final URL url = result.nextElement();
            final ClassVisitor cv = visitor.visitClass( url );
            if ( null != cv )
            {
                accept( cv, url );
            }
        }

        visitor.visitEnd();
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
                new ClassReader( in ).accept( visitor, ASM_FLAGS );
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
            Logs.debug( "Problem scanning: {}", url, e );
        }
    }

    public static boolean verify( final ClassSpace space, final Class<?>... specification )
    {
        for ( final Class<?> expectedClazz : specification )
        {
            try
            {
                final Class<?> spaceClazz = space.loadClass( expectedClazz.getName() );
                if ( spaceClazz != expectedClazz )
                {
                    Logs.warn( "Inconsistent ClassLoader for: {} in: {}", expectedClazz, space );
                    Logs.warn( "Expected: {} saw: {}", expectedClazz.getClassLoader(), spaceClazz.getClassLoader() );
                }
            }
            catch ( final TypeNotPresentException e )
            {
                if ( expectedClazz.isAnnotation() )
                {
                    Logs.debug( "Potential problem: {} is not visible from: {}", expectedClazz, space );
                }
            }
        }
        return true;
    }
}
