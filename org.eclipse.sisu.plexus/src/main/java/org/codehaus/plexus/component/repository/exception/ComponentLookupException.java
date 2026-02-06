/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.codehaus.plexus.component.repository.exception;

public final class ComponentLookupException extends Exception {
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty("line.separator");

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String role;

    private final String hint;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ComponentLookupException(final String message, final String role, final String hint) {
        super(message);
        this.role = role;
        this.hint = hint;
    }

    public ComponentLookupException(final Throwable cause, final String role, final String hint) {
        super(cause);
        this.role = role;
        this.hint = hint;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + LS + "      role: " + role + LS + "  roleHint: " + hint;
    }
}
