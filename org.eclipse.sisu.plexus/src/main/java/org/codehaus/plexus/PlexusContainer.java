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
package org.codehaus.plexus;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.Context;

public interface PlexusContainer
{
    Context getContext();

    Object lookup( String role )
        throws ComponentLookupException;

    Object lookup( String role, String hint )
        throws ComponentLookupException;

    <T> T lookup( Class<T> role )
        throws ComponentLookupException;

    <T> T lookup( Class<T> role, String hint )
        throws ComponentLookupException;

    <T> T lookup( Class<T> type, String role, String hint )
        throws ComponentLookupException;

    List<Object> lookupList( String role )
        throws ComponentLookupException;

    <T> List<T> lookupList( Class<T> role )
        throws ComponentLookupException;

    Map<String, Object> lookupMap( String role )
        throws ComponentLookupException;

    <T> Map<String, T> lookupMap( Class<T> role )
        throws ComponentLookupException;

    boolean hasComponent( String role );

    boolean hasComponent( String role, String hint );

    boolean hasComponent( Class<?> role );

    boolean hasComponent( Class<?> role, String hint );

    boolean hasComponent( Class<?> type, String role, String hint );

    void addComponent( Object component, String role );

    <T> void addComponent( T component, Class<?> role, String hint );

    <T> void addComponentDescriptor( ComponentDescriptor<T> descriptor )
        throws CycleDetectedInComponentGraphException;

    ComponentDescriptor<?> getComponentDescriptor( String role, String hint );

    <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type, String role, String hint );

    List<ComponentDescriptor<?>> getComponentDescriptorList( String role );

    <T> List<ComponentDescriptor<T>> getComponentDescriptorList( Class<T> type, String role );

    Map<String, ComponentDescriptor<?>> getComponentDescriptorMap( String role );

    <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( Class<T> type, String role );

    List<ComponentDescriptor<?>> discoverComponents( ClassRealm classRealm )
        throws PlexusConfigurationException;

    ClassRealm getContainerRealm();

    ClassRealm setLookupRealm( ClassRealm realm );

    ClassRealm getLookupRealm();

    ClassRealm createChildRealm( String id );

    void release( Object component )
        throws ComponentLifecycleException;

    void releaseAll( Map<String, ?> components )
        throws ComponentLifecycleException;

    void releaseAll( List<?> components )
        throws ComponentLifecycleException;

    void dispose();
}
