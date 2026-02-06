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
package org.codehaus.plexus.component.repository;

public class ComponentRequirement {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String role;

    private String hint = "";

    private String name;

    private boolean optional;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setRole(final String role) {
        this.role = role;
    }

    public final void setRoleHint(final String hint) {
        // empty/null hint represents wildcard
        this.hint = null != hint ? hint : "";
    }

    public final void setFieldName(final String name) {
        this.name = name;
    }

    public final void setOptional(final boolean optional) {
        this.optional = optional;
    }

    @SuppressWarnings("unused")
    public final void setFieldMappingType(final String mappingType) {
        // ignore
    }

    public final String getRole() {
        return role;
    }

    public final String getRoleHint() {
        return hint;
    }

    public final String getFieldName() {
        return name;
    }

    public final boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return "ComponentRequirement{role='" + role + "', roleHint='" + hint + "', fieldName='" + name + "'}";
    }

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof ComponentRequirement) {
            return id().equals(((ComponentRequirement) rhs).id());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final String id() {
        return role + ':' + hint;
    }
}
