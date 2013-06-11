/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Qualifier;

import org.eclipse.sisu.BeanScanning;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.ProviderLookup;

/**
 * Guice {@link Module} that automatically binds types annotated with {@link Qualifier} annotations.
 */
public class SpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static ConcurrentMap<String, List<Element>> cachedElementsMap;

    private final boolean caching;

    final ClassSpace space;

    final ClassFinder finder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.ON );
    }

    public SpaceModule( final ClassSpace space, final ClassFinder finder )
    {
        caching = false;

        this.space = space;
        this.finder = finder;
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        caching = BeanScanning.CACHE == scanning;

        this.space = space;
        switch ( scanning )
        {
            case OFF:
                finder = null;
                break;
            case INDEX:
                finder = new SisuIndexClassFinder( false );
                break;
            case GLOBAL_INDEX:
                finder = new SisuIndexClassFinder( true );
                break;
            default:
                finder = new DefaultClassFinder();
                break;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void configure( final Binder binder )
    {
        binder.bind( ClassSpace.class ).toInstance( space );

        if ( caching )
        {
            recordAndReplayElements( binder );
        }
        else if ( null != finder )
        {
            new ClassSpaceScanner( finder, space ).accept( visitor( binder ) );
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected ClassSpaceVisitor visitor( final Binder binder )
    {
        return new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final void recordAndReplayElements( final Binder binder )
    {
        synchronized ( SpaceModule.class )
        {
            if ( null == cachedElementsMap )
            {
                cachedElementsMap = new ConcurrentHashMap<String, List<Element>>();
            }
        }

        boolean alreadyCached = true;

        /*
         * Record elements first time round
         */
        final String key = space.toString();
        List<Element> cachedElements = cachedElementsMap.get( key );
        if ( null == cachedElements )
        {
            final List<Element> elements = Elements.getElements( new Module()
            {
                public void configure( final Binder recorder )
                {
                    new ClassSpaceScanner( finder, space ).accept( visitor( recorder ) );
                }
            } );
            cachedElements = cachedElementsMap.putIfAbsent( key, elements );
            if ( null == cachedElements )
            {
                cachedElements = elements;
                alreadyCached = false;
            }
        }

        /*
         * Then replay onto current binder
         */
        for ( final Element e : cachedElements )
        {
            if ( alreadyCached )
            {
                // lookups have state so we replace them with duplicates when replaying...
                if ( e instanceof ProviderLookup<?> )
                {
                    binder.getProvider( ( (ProviderLookup<?>) e ).getKey() );
                    continue;
                }
                if ( e instanceof MembersInjectorLookup<?> )
                {
                    binder.getMembersInjector( ( (MembersInjectorLookup<?>) e ).getType() );
                    continue;
                }
            }
            e.applyTo( binder );
        }
    }
}
