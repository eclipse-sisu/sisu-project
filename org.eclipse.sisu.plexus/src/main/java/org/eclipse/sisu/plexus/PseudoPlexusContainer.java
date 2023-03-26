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

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

/**
 * Delegating {@link PlexusContainer} wrapper that doesn't require an actual container instance.
 */
@Singleton
@SuppressWarnings( { "unchecked", "rawtypes" } )
final class PseudoPlexusContainer
    implements PlexusContainer
{
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
    PseudoPlexusContainer( final PlexusBeanLocator locator, final BeanManager manager, final Context context )
    {
        this.locator = locator;
        this.manager = manager;
        this.context = context;
    }

    // ----------------------------------------------------------------------
    // Context methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        return context;
    }

    // ----------------------------------------------------------------------
    // Lookup methods
    // ----------------------------------------------------------------------

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return lookup( role, "" );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return lookup( null, role, hint );
    }

    public <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        return lookup( role, "" );
    }

    public <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        return lookup( role, null, hint );
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return locate( role, type, hint ).iterator().next().getValue();
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentLookupException( e, null != role ? role : type.getName(), hint );
        }
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return new EntryListAdapter<Object>( locate( role, null ) );
    }

    public <T> List<T> lookupList( final Class<T> role )
        throws ComponentLookupException
    {
        return new EntryListAdapter<T>( locate( null, role ) );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return new EntryMapAdapter<String, Object>( locate( role, null ) );
    }

    public <T> Map<String, T> lookupMap( final Class<T> role )
        throws ComponentLookupException
    {
        return new EntryMapAdapter<String, T>( locate( null, role ) );
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    public boolean hasComponent( final String role )
    {
        return hasComponent( role, "" );
    }

    public boolean hasComponent( final String role, final String hint )
    {
        return hasComponent( null, role, hint );
    }

    public boolean hasComponent( final Class role )
    {
        return hasComponent( role, "" );
    }

    public boolean hasComponent( final Class role, final String hint )
    {
        return hasComponent( role, null, hint );
    }

    public boolean hasComponent( final Class type, final String role, final String hint )
    {
        return hasPlexusBeans( locate( role, type, hint ) );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public void addComponent( final Object component, final String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> void addComponent( final T component, final Class<?> role, final String hint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role,
                                                              final String hint )
    {
        throw new UnsupportedOperationException();
    }

    public List getComponentDescriptorList( final String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        throw new UnsupportedOperationException();
    }

    public Map getComponentDescriptorMap( final String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( final Class<T> type, final String role )
    {
        throw new UnsupportedOperationException();
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    public ClassRealm getContainerRealm()
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm setLookupRealm( final ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getLookupRealm()
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm createChildRealm( final String id )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    public void release( final Object component )
    {
        manager.unmanage( component );
    }

    public void releaseAll( final Map<String, ?> components )
    {
        for ( final Object o : components.values() )
        {
            release( o );
        }
    }

    public void releaseAll( final List<?> components )
    {
        for ( final Object o : components )
        {
            release( o );
        }
    }

    public void dispose()
    {
        manager.unmanage();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private <T> Iterable<PlexusBean<T>> locate( final String role, final Class<T> type, final String... hints )
    {
        final String[] canonicalHints = Hints.canonicalHints( hints );
        if ( null == role || null != type && type.getName().equals( role ) )
        {
            return locator.locate( TypeLiteral.get( type ), canonicalHints );
        }
        try
        {
            final Class clazz = Thread.currentThread().getContextClassLoader().loadClass( role );
            final Iterable beans = locator.locate( TypeLiteral.get( clazz ), canonicalHints );
            if ( hasPlexusBeans( beans ) )
            {
                return beans;
            }
        }
        catch ( final Exception e )
        {
            // drop through...
        }
        catch ( final LinkageError e )
        {
            // drop through...
        }
        return Collections.EMPTY_SET;
    }

    private static <T> boolean hasPlexusBeans( final Iterable<PlexusBean<T>> beans )
    {
        final Iterator<PlexusBean<T>> i = beans.iterator();
        return i.hasNext() && i.next().getImplementationClass() != null;
    }
}
