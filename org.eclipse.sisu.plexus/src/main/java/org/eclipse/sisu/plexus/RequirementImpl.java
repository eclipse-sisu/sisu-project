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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.space.LoadedClass;

/**
 * Runtime implementation of Plexus @{@link Requirement} annotation.
 */
public final class RequirementImpl
    implements Requirement
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final DeferredClass<?> role;

    private final boolean optional;

    private final String hint;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RequirementImpl( final DeferredClass<?> role, final boolean optional, final List<String> hints )
    {
        if ( null == role || null == hints || hints.contains( null ) )
        {
            throw new IllegalArgumentException( "@Requirement cannot contain null values" );
        }

        this.role = role;
        this.optional = optional;

        final int length = hints.size();
        if ( length == 0 )
        {
            hint = "";
            this.hints = Hints.NO_HINTS;
        }
        else if ( length == 1 )
        {
            hint = hints.get( 0 );
            this.hints = Hints.NO_HINTS;
        }
        else
        {
            hint = "";
            this.hints = hints.toArray( new String[length] );
        }
    }

    /**
     * For testing purposes only.
     */
    @Deprecated
    public RequirementImpl( final Class<?> role, final boolean optional, final String... hints )
    {
        this( new LoadedClass<Object>( role ), optional, Arrays.asList( hints ) );
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public Class<?> role()
    {
        return role.load();
    }

    public boolean optional()
    {
        return optional;
    }

    public String hint()
    {
        return hint;
    }

    public String[] hints()
    {
        return hints.clone();
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }

        if ( rhs instanceof Requirement )
        {
            final Requirement req = (Requirement) rhs;

            return role().equals( req.role() ) && optional == req.optional() && hint.equals( req.hint() )
                && Arrays.equals( hints, req.hints() );
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "role".hashCode() ^ role().hashCode() )
            + ( 127 * "optional".hashCode() ^ Boolean.valueOf( optional ).hashCode() )
            + ( 127 * "hint".hashCode() ^ hint.hashCode() ) + ( 127 * "hints".hashCode() ^ Arrays.hashCode( hints ) );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(hints=%s, optional=%b, role=%s, hint=%s)", Requirement.class.getName(),
                              Arrays.toString( hints ), Boolean.valueOf( optional ), role(), hint );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Requirement.class;
    }
}
