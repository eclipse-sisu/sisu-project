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
package org.eclipse.sisu.plexus;

import java.lang.annotation.Annotation;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Partial runtime implementation of Plexus @{@link Component} annotation, supporting the most common attributes.
 */
public final class ComponentImpl implements Component {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    // computed hashCode representing hard-coded attributes
    private static final int HASH_CODE_OFFSET = 0x685499F5;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> role;

    private final String hint;

    private final String instantiationStrategy;

    private final String description;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentImpl(
            final Class<?> role, final String hint, final String instantiationStrategy, final String description) {
        if (null == role || null == hint || null == instantiationStrategy || null == description) {
            throw new IllegalArgumentException("@Component cannot contain null values");
        }

        this.role = role;
        this.hint = hint;
        this.instantiationStrategy = instantiationStrategy;
        this.description = description;
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    @Override
    public Class<?> role() {
        return role;
    }

    @Override
    public String hint() {
        return hint;
    }

    @Override
    public String instantiationStrategy() {
        return instantiationStrategy;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean isolatedRealm() {
        return false;
    }

    @Override
    public String alias() {
        return "";
    }

    @Override
    public String composer() {
        return "";
    }

    @Override
    public String configurator() {
        return "";
    }

    @Override
    public String factory() {
        return "";
    }

    @Override
    public String lifecycleHandler() {
        return "";
    }

    @Override
    public String profile() {
        return "";
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String version() {
        return "";
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }

        if (rhs instanceof Component) {
            final Component cmp = (Component) rhs;

            if (role.equals(cmp.role())
                    && hint.equals(cmp.hint())
                    && instantiationStrategy.equals(cmp.instantiationStrategy())
                    && description.equals(cmp.description())) {
                // optimization: we hard-code all these attributes to be empty
                final String hardCodedAttributes = cmp.alias()
                        + cmp.composer()
                        + cmp.configurator()
                        + cmp.factory()
                        + cmp.lifecycleHandler()
                        + cmp.profile()
                        + cmp.type()
                        + cmp.version();

                return hardCodedAttributes.length() == 0 && !cmp.isolatedRealm();
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return HASH_CODE_OFFSET
                + (127 * "role".hashCode() ^ role.hashCode())
                + (127 * "hint".hashCode() ^ hint.hashCode())
                + (127 * "instantiationStrategy".hashCode() ^ instantiationStrategy.hashCode())
                + (127 * "description".hashCode() ^ description.hashCode());
    }

    @Override
    public String toString() {
        return String.format(
                "@%s(isolatedRealm=false, composer=, configurator=, alias=, description=%s, "
                        + "instantiationStrategy=%s, factory=, hint=%s, type=, lifecycleHandler=, version=, "
                        + "profile=, role=%s)",
                Component.class.getName(), description, instantiationStrategy, hint, role);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Component.class;
    }
}
