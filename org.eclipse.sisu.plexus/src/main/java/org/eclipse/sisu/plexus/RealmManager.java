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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.classworlds.ClassWorldListener;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.sisu.inject.InjectorBindings;
import org.eclipse.sisu.inject.MutableBeanLocator;

import com.google.inject.Injector;

/**
 * Manages {@link ClassRealm} associated data for the Plexus container.
 */
public final class RealmManager
    implements ClassWorldListener
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean getImportRealmsSupported = true;
        try
        {
            // support both old and new forms of Plexus class realms
            ClassRealm.class.getDeclaredMethod( "getImportRealms" );
        }
        catch ( final Exception e )
        {
            getImportRealmsSupported = false;
        }
        catch ( final LinkageError e )
        {
            getImportRealmsSupported = false;
        }
        GET_IMPORT_REALMS_SUPPORTED = getImportRealmsSupported;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean GET_IMPORT_REALMS_SUPPORTED;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<ClassRealm, Set<String>> visibility =
        new ConcurrentHashMap<ClassRealm, Set<String>>( 16, 0.75f, 1 );

    private final ConcurrentMap<ClassRealm, Injector> injectors =
        new ConcurrentHashMap<ClassRealm, Injector>( 16, 0.75f, 1 );

    private final MutableBeanLocator beanLocator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RealmManager( final MutableBeanLocator beanLocator )
    {
        this.beanLocator = beanLocator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Current context realm
     */
    public static ClassRealm contextRealm()
    {
        for ( ClassLoader tccl = Thread.currentThread().getContextClassLoader(); tccl != null; tccl = tccl.getParent() )
        {
            if ( tccl instanceof ClassRealm )
            {
                return (ClassRealm) tccl;
            }
        }
        return null;
    }

    /**
     * Walks the {@link ClassRealm} import graph to find all realms visible from the given realm.
     * 
     * @param contextRealm The initial realm
     * @return Names of all realms visible from the given realm
     */
    public Set<String> visibleRealmNames( final ClassRealm contextRealm )
    {
        if ( GET_IMPORT_REALMS_SUPPORTED && null != contextRealm )
        {
            Set<String> names = visibility.get( contextRealm );
            if ( null == names )
            {
                visibility.putIfAbsent( contextRealm, names = computeVisibleNames( contextRealm ) );
            }
            return names;
        }
        return null;
    }

    /**
     * @return {@code true} if the realm is already managed, otherwise {@code false}
     */
    public boolean isManaged( final ClassRealm realm )
    {
        return injectors.containsKey( realm );
    }

    /**
     * Manages the association between the given realm and its injector.
     * 
     * @param realm The realm
     * @param injector The injector
     */
    public void manage( final ClassRealm realm, final Injector injector )
    {
        injectors.putIfAbsent( realm, injector );
    }

    public void realmCreated( final ClassRealm realm )
    {
        // nothing to do
    }

    public void realmDisposed( final ClassRealm realm )
    {
        visibility.remove( realm );
        final Injector injector = injectors.remove( realm );
        if ( null != injector )
        {
            beanLocator.remove( new InjectorBindings( injector, null /* unused */ ) );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static Set<String> computeVisibleNames( final ClassRealm forRealm )
    {
        final Set<String> visibleRealmNames = new HashSet<String>();
        final List<ClassRealm> searchRealms = new ArrayList<ClassRealm>();

        searchRealms.add( forRealm );
        for ( int i = 0; i < searchRealms.size(); i++ )
        {
            final ClassRealm realm = searchRealms.get( i );
            if ( visibleRealmNames.add( realm.toString() ) )
            {
                searchRealms.addAll( realm.getImportRealms() );
                final ClassRealm parent = realm.getParentRealm();
                if ( null != parent )
                {
                    searchRealms.add( parent );
                }
            }
        }
        return visibleRealmNames;
    }
}
