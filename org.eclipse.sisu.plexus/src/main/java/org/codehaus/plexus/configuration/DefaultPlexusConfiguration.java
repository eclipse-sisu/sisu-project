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
package org.codehaus.plexus.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultPlexusConfiguration implements PlexusConfiguration {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final PlexusConfiguration[] NO_CHILDREN = new PlexusConfiguration[0];

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String name;

    private String value;

    private List<PlexusConfiguration> childIndex = Collections.emptyList();

    private Map<String, List<PlexusConfiguration>> childMap = Collections.emptyMap();

    private Map<String, String> attributeMap = Collections.emptyMap();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusConfiguration(final String name) {
        this(name, null);
    }

    public DefaultPlexusConfiguration(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getValue() {
        return value;
    }

    @Override
    public final String getValue(final String defaultValue) {
        return null != value ? value : defaultValue;
    }

    @Override
    public final void setValue(final String value) {
        this.value = value;
    }

    @Override
    public final String[] getAttributeNames() {
        return attributeMap.keySet().toArray(new String[attributeMap.size()]);
    }

    @Override
    public final String getAttribute(final String attributeName) {
        return attributeMap.get(attributeName);
    }

    @Override
    public final String getAttribute(final String attributeName, final String defaultValue) {
        final String attributeValue = attributeMap.get(attributeName);
        return null != attributeValue ? attributeValue : defaultValue;
    }

    @Override
    public final void setAttribute(final String attributeName, final String attributeValue) {
        if (attributeMap.isEmpty()) {
            attributeMap = new HashMap<>();
        }
        attributeMap.put(attributeName, attributeValue);
    }

    @Override
    public final PlexusConfiguration getChild(final String childName) {
        return getChild(childName, true);
    }

    @Override
    public final PlexusConfiguration getChild(final String childName, final boolean create) {
        final List<PlexusConfiguration> children = childMap.get(childName);
        if (null != children) {
            return children.get(0);
        }
        return create ? add(createChild(childName)) : null;
    }

    @Override
    public final PlexusConfiguration[] getChildren() {
        return childIndex.toArray(new PlexusConfiguration[childIndex.size()]);
    }

    @Override
    public final PlexusConfiguration[] getChildren(final String childName) {
        final List<PlexusConfiguration> children = childMap.get(childName);
        if (null != children) {
            return children.toArray(new PlexusConfiguration[children.size()]);
        }
        return NO_CHILDREN;
    }

    @Override
    public final int getChildCount() {
        return childIndex.size();
    }

    @Override
    public final PlexusConfiguration getChild(final int index) {
        return childIndex.get(index);
    }

    @Override
    public final void addChild(final PlexusConfiguration child) {
        add(child);
    }

    @Override
    public final PlexusConfiguration addChild(final String childName, final String childValue) {
        add(createChild(childName)).setValue(childValue);
        return this;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected PlexusConfiguration createChild(final String childName) {
        return new DefaultPlexusConfiguration(childName);
    }

    // ----------------------------------------------------------------------
    // Shared methods
    // ----------------------------------------------------------------------

    protected final PlexusConfiguration add(final PlexusConfiguration child) {
        final String childName = child.getName();

        List<PlexusConfiguration> children = childMap.get(childName);
        if (null == children) {
            children = new ArrayList<>();
            if (childMap.isEmpty()) {
                // create mutable map and index at the same time
                childMap = new LinkedHashMap<>();
                childIndex = new ArrayList<>();
            }
            childMap.put(childName, children);
        }

        childIndex.add(child);
        children.add(child);

        return child;
    }
}
