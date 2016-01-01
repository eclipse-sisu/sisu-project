/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component.repository.exception;

public final class ComponentLookupException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String role;

    private final String hint;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ComponentLookupException( final String message, final String role, final String hint )
    {
        super( message );
        this.role = role;
        this.hint = hint;
    }

    public ComponentLookupException( final Throwable cause, final String role, final String hint )
    {
        super( cause );
        this.role = role;
        this.hint = hint;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + LS + "      role: " + role + LS + "  roleHint: " + hint;
    }
}
