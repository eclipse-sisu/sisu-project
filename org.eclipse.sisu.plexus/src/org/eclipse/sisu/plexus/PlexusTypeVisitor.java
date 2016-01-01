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
package org.eclipse.sisu.plexus;

import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.AnnotationVisitor;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.ClassVisitor;
import org.eclipse.sisu.space.LoadedClass;
import org.eclipse.sisu.space.QualifiedTypeVisitor;
import org.eclipse.sisu.space.SpaceScanner;
import org.eclipse.sisu.space.SpaceVisitor;

/**
 * {@link SpaceVisitor} that reports Plexus bean classes annotated with @{@link Component}.
 */
public final class PlexusTypeVisitor
    implements SpaceVisitor, ClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String COMPONENT_DESC = SpaceScanner.jvmDescriptor( Component.class );

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

    public void enterSpace( final ClassSpace _space )
    {
        space = _space;
        source = _space.toString();
        qualifiedTypeVisitor.enterSpace( _space );

        if ( Logs.TRACE_ENABLED )
        {
            QualifiedTypeVisitor.verify( _space, Component.class );
        }
    }

    public ClassVisitor visitClass( final URL url )
    {
        componentVisitor.reset();
        implementation = null;
        qualifiedTypeVisitor.visitClass( null ); // disable detailed source location (see realm filtering)
        return this;
    }

    public void enterClass( final int modifiers, final String name, final String _extends, final String[] _implements )
    {
        if ( ( modifiers & NON_INSTANTIABLE ) == 0 )
        {
            implementation = name;
        }
        qualifiedTypeVisitor.enterClass( modifiers, name, _extends, _implements );
    }

    public AnnotationVisitor visitAnnotation( final String desc )
    {
        if ( COMPONENT_DESC.equals( desc ) )
        {
            return componentVisitor;
        }
        return qualifiedTypeVisitor.visitAnnotation( desc );
    }

    public void leaveClass()
    {
        if ( null != implementation )
        {
            final Component component = componentVisitor.getComponent( space );
            if ( null != component )
            {
                final Class<?> clazz = space.loadClass( implementation.replace( '/', '.' ) );
                plexusTypeListener.hear( component, new LoadedClass<Object>( clazz ), source );
                qualifiedTypeVisitor.disqualify();
            }
        }
        qualifiedTypeVisitor.leaveClass();
    }

    public void leaveSpace()
    {
        qualifiedTypeVisitor.leaveSpace();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link AnnotationVisitor} that records details of @{@link Component} annotations.
     */
    static final class ComponentAnnotationVisitor
        implements AnnotationVisitor
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private String role;

        private String hint;

        private String strategy;

        private String description;

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void reset()
        {
            role = null;
            hint = Hints.DEFAULT_HINT;
            strategy = Strategies.SINGLETON;
            description = "";
        }

        public void enterAnnotation()
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

        public void leaveAnnotation()
        {
            // no-op; maintain results outside of individual annotation scan
        }

        public Component getComponent( final ClassSpace space )
        {
            return null != role ? new ComponentImpl( space.loadClass( role ), hint, strategy, description ) : null;
        }
    }
}
