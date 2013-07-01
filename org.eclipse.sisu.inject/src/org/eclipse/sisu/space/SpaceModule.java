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

import java.net.URL;
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

    private static final class RecordedElements
    {
        static final ConcurrentMap<String, List<Element>> cache = new ConcurrentHashMap<String, List<Element>>();
    }

    private final boolean caching;

    private final ClassSpace space;

    private final ClassFinder finder;

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
            scanForElements( binder );
        }
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected SpaceVisitor visitor( final Binder binder )
    {
        return new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) );
    }

    /**
     * @param binder
     */
    protected List<SpaceVisitor> extensions( final Binder binder )
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    void scanForElements( final Binder binder )
    {
        final SpaceScanner scanner = new SpaceScanner( finder, space );

        final SpaceVisitor defaultVisitor = visitor( binder );
        final List<SpaceVisitor> customVisitors = extensions( binder );
        if ( null == customVisitors || customVisitors.isEmpty() )
        {
            scanner.accept( defaultVisitor );
        }
        else
        {
            scanner.accept( new SpaceVisitor()
            {
                public void enterSpace( final ClassSpace _space )
                {
                    for ( final SpaceVisitor v : customVisitors )
                    {
                        v.enterSpace( _space );
                    }
                    defaultVisitor.enterSpace( _space );
                }

                public ClassVisitor visitClass( final URL url )
                {
                    for ( final SpaceVisitor v : customVisitors )
                    {
                        final ClassVisitor visited = v.visitClass( url );
                        if ( null != visited )
                        {
                            return visited;
                        }
                    }
                    return defaultVisitor.visitClass( url );
                }

                public void leaveSpace()
                {
                    for ( final SpaceVisitor v : customVisitors )
                    {
                        v.leaveSpace();
                    }
                    defaultVisitor.leaveSpace();
                }
            } );
        }
    }

    private void recordAndReplayElements( final Binder binder )
    {
        boolean alreadyUsed = true;

        /*
         * Record elements first time round
         */
        final String key = space.toString();
        List<Element> elements = RecordedElements.cache.get( key );
        if ( null == elements )
        {
            final List<Element> recording = Elements.getElements( new Module()
            {
                public void configure( final Binder recorder )
                {
                    scanForElements( recorder );
                }
            } );
            elements = RecordedElements.cache.putIfAbsent( key, recording );
            if ( null == elements )
            {
                elements = recording;
                alreadyUsed = false;
            }
        }

        /*
         * Then replay onto current binder
         */
        for ( final Element e : elements )
        {
            if ( alreadyUsed )
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
