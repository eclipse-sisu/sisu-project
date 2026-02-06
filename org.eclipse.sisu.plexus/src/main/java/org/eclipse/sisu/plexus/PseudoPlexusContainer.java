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

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.EntryMapAdapter;

/**
 * Delegating {@link PlexusContainer} wrapper that doesn't require an actual container instance.
 */
@Singleton
@SuppressWarnings({"unchecked", "rawtypes"})
final class PseudoPlexusContainer implements PlexusContainer {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final PlexusBeanLocator locator;

    final BeanManager manager;

    final Context context;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    PseudoPlexusContainer(final PlexusBeanLocator locator, final BeanManager manager, final Context context) {
        this.locator = locator;
        this.manager = manager;
        this.context = context;
    }

    // ----------------------------------------------------------------------
    // Context methods
    // ----------------------------------------------------------------------

    @Override
    public Context getContext() {
        return context;
    }

    // ----------------------------------------------------------------------
    // Lookup methods
    // ----------------------------------------------------------------------

    @Override
    public Object lookup(final String role) throws ComponentLookupException {
        return lookup(role, "");
    }

    @Override
    public Object lookup(final String role, final String hint) throws ComponentLookupException {
        return lookup(null, role, hint);
    }

    @Override
    public <T> T lookup(final Class<T> role) throws ComponentLookupException {
        return lookup(role, "");
    }

    @Override
    public <T> T lookup(final Class<T> role, final String hint) throws ComponentLookupException {
        return lookup(role, null, hint);
    }

    @Override
    public <T> T lookup(final Class<T> type, final String role, final String hint) throws ComponentLookupException {
        try {
            return locate(role, type, hint).iterator().next().getValue();
        } catch (final RuntimeException e) {
            throw new ComponentLookupException(e, null != role ? role : type.getName(), hint);
        }
    }

    @Override
    public List<Object> lookupList(final String role) throws ComponentLookupException {
        return new EntryListAdapter<>(locate(role, null));
    }

    @Override
    public <T> List<T> lookupList(final Class<T> role) throws ComponentLookupException {
        return new EntryListAdapter<>(locate(null, role));
    }

    @Override
    public Map<String, Object> lookupMap(final String role) throws ComponentLookupException {
        return new EntryMapAdapter<>(locate(role, null));
    }

    @Override
    public <T> Map<String, T> lookupMap(final Class<T> role) throws ComponentLookupException {
        return new EntryMapAdapter<>(locate(null, role));
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    @Override
    public boolean hasComponent(final String role) {
        return hasComponent(role, "");
    }

    @Override
    public boolean hasComponent(final String role, final String hint) {
        return hasComponent(null, role, hint);
    }

    @Override
    public boolean hasComponent(final Class role) {
        return hasComponent(role, "");
    }

    @Override
    public boolean hasComponent(final Class role, final String hint) {
        return hasComponent(role, null, hint);
    }

    @Override
    public boolean hasComponent(final Class type, final String role, final String hint) {
        return hasPlexusBeans(locate(role, type, hint));
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    @Override
    public void addComponent(final Object component, final String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void addComponent(final T component, final Class<?> role, final String hint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void addComponentDescriptor(final ComponentDescriptor<T> descriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptor(final String role, final String hint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ComponentDescriptor<T> getComponentDescriptor(
            final Class<T> type, final String role, final String hint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List getComponentDescriptorList(final String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(final Class<T> type, final String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map getComponentDescriptorMap(final String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap(final Class<T> type, final String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ComponentDescriptor<?>> discoverComponents(final ClassRealm realm) {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    @Override
    public ClassRealm getContainerRealm() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassRealm setLookupRealm(final ClassRealm realm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassRealm getLookupRealm() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassRealm createChildRealm(final String id) {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    @Override
    public void release(final Object component) {
        manager.unmanage(component);
    }

    @Override
    public void releaseAll(final Map<String, ?> components) {
        for (final Object o : components.values()) {
            release(o);
        }
    }

    @Override
    public void releaseAll(final List<?> components) {
        for (final Object o : components) {
            release(o);
        }
    }

    @Override
    public void dispose() {
        manager.unmanage();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private <T> Iterable<PlexusBean<T>> locate(final String role, final Class<T> type, final String... hints) {
        final String[] canonicalHints = Hints.canonicalHints(hints);
        if (null == role || null != type && type.getName().equals(role)) {
            return locator.locate(TypeLiteral.get(type), canonicalHints);
        }
        try {
            final Class clazz = Thread.currentThread().getContextClassLoader().loadClass(role);
            final Iterable beans = locator.locate(TypeLiteral.get(clazz), canonicalHints);
            if (hasPlexusBeans(beans)) {
                return beans;
            }
        } catch (final LinkageError | Exception e) {
            // drop through...
        }
        return Collections.EMPTY_SET;
    }

    private static <T> boolean hasPlexusBeans(final Iterable<PlexusBean<T>> beans) {
        final Iterator<PlexusBean<T>> i = beans.iterator();
        return i.hasNext() && i.next().getImplementationClass() != null;
    }
}
