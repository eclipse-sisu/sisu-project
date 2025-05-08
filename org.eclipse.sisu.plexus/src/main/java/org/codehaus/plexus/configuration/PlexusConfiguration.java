/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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

public interface PlexusConfiguration
{
    String getName();

    String getValue();

    String getValue( String defaultValue );

    void setValue( String value );

    String[] getAttributeNames();

    String getAttribute( String attributeName );

    String getAttribute( String attributeName, String defaultValue );

    void setAttribute( String name, String value );

    PlexusConfiguration getChild( String childName );

    PlexusConfiguration getChild( String childName, boolean create );

    PlexusConfiguration[] getChildren();

    PlexusConfiguration[] getChildren( String childName );

    int getChildCount();

    PlexusConfiguration getChild( int index );

    void addChild( PlexusConfiguration child );

    PlexusConfiguration addChild( String name, String value );
}
