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
package org.eclipse.sisu.space;

import java.net.URL;

import javax.inject.Qualifier;

import org.eclipse.sisu.inject.Logs;

import com.google.inject.Module;

/**
 * {@link SpaceVisitor} that reports types annotated with {@link Qualifier} annotations.
 */
public final class QualifiedTypeVisitor
    implements SpaceVisitor, ClassVisitor
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifierCache qualifierCache = new QualifierCache();

    private final QualifiedTypeListener listener;

    private ClassSpace space;

    private URL location;

    private String source;

    private String clazzName;

    private boolean qualified;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedTypeVisitor( final QualifiedTypeListener listener )
    {
        this.listener = listener;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

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

    public void enterSpace( final ClassSpace _space )
    {
        space = _space;
        source = null;

        if ( Logs.TRACE_ENABLED )
        {
            verify( _space, Qualifier.class, Module.class );
        }
    }

    public ClassVisitor visitClass( final URL url )
    {
        location = url;
        clazzName = null;
        qualified = false;

        return this;
    }

    public void enterClass( final int modifiers, final String name, final String _extends, final String[] _implements )
    {
        if ( ( modifiers & NON_INSTANTIABLE ) == 0 )
        {
            clazzName = name; // concrete type
        }
    }

    public AnnotationVisitor visitAnnotation( final String desc )
    {
        if ( null != clazzName )
        {
            qualified = qualified || qualifierCache.qualify( space, desc );
        }
        return null;
    }

    public void disqualify()
    {
        qualified = false;
    }

    public void leaveClass()
    {
        if ( qualified )
        {
            listener.hear( space.loadClass( clazzName.replace( '/', '.' ) ), findSource() );
        }
    }

    public void leaveSpace()
    {
        // no-op
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Finds source of current class; detailed location or {@link ClassSpace#toString()}.
     */
    private String findSource()
    {
        if ( null != location )
        {
            // compressed record of class location
            final String path = location.getPath();
            if ( null == source || !path.startsWith( source ) )
            {
                final int i = path.indexOf( clazzName );
                source = i <= 0 ? path : path.substring( 0, i );
            }
        }
        else if ( null == source )
        {
            source = space.toString();
        }
        return source;
    }
}
