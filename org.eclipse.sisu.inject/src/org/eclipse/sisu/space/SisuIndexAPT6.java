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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Java 6 Annotation {@link Processor} that generates a qualified class index for the current build.
 * <p>
 * The index consists of qualified class names listed in {@code META-INF/sisu/javax.inject.Named}.
 * 
 * @see <a href="http://eclipse.org/sisu/docs/api/org.eclipse.sisu.mojos/">sisu-maven-plugin</a>
 */
public final class SisuIndexAPT6
    extends AbstractSisuIndex
    implements Processor
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean hasQualifier;
        try
        {
            hasQualifier = javax.inject.Qualifier.class.isAnnotation();
        }
        catch ( final LinkageError e )
        {
            hasQualifier = false;
        }
        HAS_QUALIFIER = hasQualifier;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String QUALIFIERS = "sisu.qualifiers";

    private static final String ALL = "all";

    private static final String NONE = "none";

    private static final boolean HAS_QUALIFIER;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private ProcessingEnvironment environment;

    private String qualifiers;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void init( final ProcessingEnvironment _environment )
    {
        environment = _environment;
        qualifiers = _environment.getOptions().get( QUALIFIERS );
        if ( null == qualifiers )
        {
            qualifiers = System.getProperty( QUALIFIERS );
        }
    }

    public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment round )
    {
        final Elements elementUtils = environment.getElementUtils();
        for ( final TypeElement anno : annotations )
        {
            if ( !ALL.equals( qualifiers ) || hasQualifier( anno ) )
            {
                for ( final Element elem : round.getElementsAnnotatedWith( anno ) )
                {
                    if ( elem.getKind().isClass() )
                    {
                        addClassToIndex( NAMED, elementUtils.getBinaryName( (TypeElement) elem ) );
                    }
                }
            }
        }

        if ( round.processingOver() )
        {
            flushIndex();
        }

        return false;
    }

    public Iterable<? extends Completion> getCompletions( final Element element, final AnnotationMirror annotation,
                                                          final ExecutableElement member, final String userText )
    {
        return Collections.emptySet();
    }

    public Set<String> getSupportedAnnotationTypes()
    {
        if ( ALL.equalsIgnoreCase( qualifiers ) )
        {
            return Collections.singleton( "*" );
        }
        if ( NONE.equalsIgnoreCase( qualifiers ) )
        {
            return Collections.emptySet();
        }
        if ( qualifiers != null && qualifiers.length() > 0 )
        {
            final Set<String> annotationTypes = new HashSet<String>();
            for ( String type : Tokens.splitByComma( qualifiers ) )
            {
                annotationTypes.add( type );
            }
            return annotationTypes;
        }
        return Collections.singleton( NAMED );
    }

    public Set<String> getSupportedOptions()
    {
        return Collections.singleton( QUALIFIERS );
    }

    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void info( final String msg )
    {
        environment.getMessager().printMessage( Diagnostic.Kind.NOTE, msg );
    }

    @Override
    protected void warn( final String msg )
    {
        environment.getMessager().printMessage( Diagnostic.Kind.WARNING, msg );
    }

    @Override
    protected Reader getReader( final String path )
        throws IOException
    {
        final FileObject file = environment.getFiler().getResource( StandardLocation.CLASS_OUTPUT, "", path );
        return new InputStreamReader( file.openInputStream(), "UTF-8" );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        return environment.getFiler().createResource( StandardLocation.CLASS_OUTPUT, "", path ).openWriter();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static boolean hasQualifier( final TypeElement anno )
    {
        if ( HAS_QUALIFIER )
        {
            return null != anno.getAnnotation( javax.inject.Qualifier.class );
        }
        for ( final AnnotationMirror mirror : anno.getAnnotationMirrors() )
        {
            if ( QUALIFIER.equals( mirror.getAnnotationType().toString() ) )
            {
                return true;
            }
        }
        return false;
    }
}
