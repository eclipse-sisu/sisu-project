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
package org.eclipse.sisu.plexus;

import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.AnnotationVisitor;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.ClassSpaceVisitor;
import org.eclipse.sisu.space.ClassVisitor;
import org.eclipse.sisu.space.LoadedClass;
import org.eclipse.sisu.space.QualifiedTypeVisitor;

/**
 * {@link ClassSpaceVisitor} that reports Plexus bean classes annotated with @{@link Component}.
 */
public final class PlexusTypeVisitor
    implements ClassSpaceVisitor, ClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String COMPONENT_DESC = 'L' + Component.class.getName().replace( '.', '/' ) + ';';

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ComponentAnnotationVisitor componentVisitor = new ComponentAnnotationVisitor();

    private final PlexusTypeListener plexusTypeListener;

    private final QualifiedTypeVisitor qualifiedTypeVisitor;

    private ClassSpace space;

    private String source;

    private String implementation;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusTypeVisitor( final PlexusTypeListener listener )
    {
        plexusTypeListener = listener;
        qualifiedTypeVisitor = new QualifiedTypeVisitor( listener );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void enter( final ClassSpace _space )
    {
        space = _space;
        source = _space.toString();
        qualifiedTypeVisitor.enter( _space );

        if ( Logs.TRACE_ENABLED )
        {
            QualifiedTypeVisitor.verify( _space, Component.class );
        }
    }

    public ClassVisitor visitClass( final URL url )
    {
        componentVisitor.reset();
        implementation = null;
        qualifiedTypeVisitor.visitClass( url );
        return this;
    }

    public void enter( final int modifiers, final String name, final String _extends, final String[] _implements )
    {
        if ( ( modifiers & NON_INSTANTIABLE ) == 0 )
        {
            implementation = name.replace( '/', '.' );
        }
        qualifiedTypeVisitor.enter( modifiers, name, _extends, _implements );
    }

    public AnnotationVisitor visitAnnotation( final String desc )
    {
        if ( COMPONENT_DESC.equals( desc ) )
        {
            return componentVisitor;
        }
        return qualifiedTypeVisitor.visitAnnotation( desc );
    }

    public void leave()
    {
        if ( null != implementation )
        {
            final Component component = componentVisitor.getComponent( space );
            if ( null != component )
            {
                final LoadedClass<?> clazz = new LoadedClass<Object>( space.loadClass( implementation ) );
                plexusTypeListener.hear( component, clazz, source );
            }
            else
            {
                qualifiedTypeVisitor.leave();
            }
            implementation = null;
        }
    }

    // ----------------------------------------------------------------------
    // Component annotation scanner
    // ----------------------------------------------------------------------

    static final class ComponentAnnotationVisitor
        implements AnnotationVisitor
    {
        private String role;

        private String hint;

        private String strategy;

        private String description;

        public void reset()
        {
            role = null;
            hint = Hints.DEFAULT_HINT;
            strategy = Strategies.SINGLETON;
            description = "";
        }

        public void enter()
        {
            // no-op; maintain results outside of individual annotation scan
        }

        public void visitElement( final String name, final Object value )
        {
            if ( "role".equals( name ) )
            {
                role = (String) value;
            }
            else if ( "hint".equals( name ) )
            {
                hint = Hints.canonicalHint( (String) value );
            }
            else if ( "instantiationStrategy".equals( name ) )
            {
                strategy = (String) value;
            }
            else if ( "description".equals( name ) )
            {
                description = (String) value;
            }
        }

        public void leave()
        {
            // no-op; maintain results outside of individual annotation scan
        }

        public Component getComponent( final ClassSpace space )
        {
            return null != role ? new ComponentImpl( space.loadClass( role ), hint, strategy, description ) : null;
        }
    }
}
