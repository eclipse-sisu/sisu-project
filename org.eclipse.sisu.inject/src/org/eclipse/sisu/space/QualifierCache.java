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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Qualifier;

/**
 * Caching {@link ClassVisitor} that maintains a map of known {@link Qualifier} annotations.
 */
final class QualifierCache
    implements ClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String QUALIFIER_DESC = "Ljavax/inject/Qualifier;";

    private static final String NAMED_DESC = "Ljavax/inject/Named;";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Map<String, Boolean> cachedResults = new ConcurrentHashMap<String, Boolean>( 32, 0.75f, 1 );

    private boolean isQualified;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void enterClass( final int modifiers, final String name, final String _extends, final String[] _implements )
    {
        // no-op
    }

    public AnnotationVisitor visitAnnotation( final String desc )
    {
        isQualified |= QUALIFIER_DESC.equals( desc );
        return null;
    }

    public void leaveClass()
    {
        // no-op
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Scans the given annotation type to see if it is marked with {@link Qualifier}.
     * 
     * @param space The class space
     * @param desc The annotation descriptor
     * @return {@code true} if the annotation is a qualifier; otherwise {@code false}
     */
    boolean qualify( final ClassSpace space, final String desc )
    {
        if ( NAMED_DESC.equals( desc ) )
        {
            return true;
        }
        final Boolean result = cachedResults.get( desc );
        if ( null == result )
        {
            isQualified = false;

            final String name = desc.substring( 1, desc.length() - 1 );
            SpaceScanner.accept( this, space.getResource( name + ".class" ) );
            cachedResults.put( desc, Boolean.valueOf( isQualified ) );

            return isQualified;
        }
        return result.booleanValue();
    }
}
