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
package org.eclipse.sisu.space;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Qualifier;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderLookup;

/**
 * Guice {@link Module} that automatically binds types annotated with {@link Qualifier} annotations.
 */
public final class SpaceModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String NAMED_INDEX = AbstractSisuIndex.INDEX_FOLDER + AbstractSisuIndex.NAMED;

    public static final ClassFinder LOCAL_INDEX = new IndexedClassFinder( NAMED_INDEX, false );

    public static final ClassFinder GLOBAL_INDEX = new IndexedClassFinder( NAMED_INDEX, true );

    public static final ClassFinder LOCAL_SCAN = SpaceScanner.DEFAULT_FINDER;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final class RecordedElements
    {
        static final ConcurrentMap<String, List<Element>> cache = //
            new ConcurrentHashMap<String, List<Element>>( 16, 0.75f, 1 );
    }

    private final boolean caching;

    private final ClassSpace space;

    private final ClassFinder finder;

    /**
     * If set to {@code true} will throw {@link RuntimeException} in case class cannot be scanned
     */
    private final boolean isStrict;

    private Strategy strategy = Strategy.DEFAULT;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * 
     * @param space
     * @deprecated Use {@link #SpaceModule(ClassSpace, ClassFinder, boolean)} instead.
     */
    @Deprecated
    public SpaceModule( final ClassSpace space )
    {
        this( space, BeanScanning.ON );
    }

    /**
     * 
     * @param space
     * @param finder
     * @deprecated Use {@link #SpaceModule(ClassSpace, ClassFinder, boolean)} instead.
     */
    @Deprecated
    public SpaceModule( final ClassSpace space, final ClassFinder finder )
    {
        this( space, finder, false );
    }

    public SpaceModule( final ClassSpace space, final ClassFinder finder, boolean isStrict )
    {
        caching = false;

        this.space = space;
        this.finder = finder;
        this.isStrict = isStrict;
    }

    /**
     * 
     * @param space
     * @param scanning
     * @deprecated Use {@link #SpaceModule(ClassSpace, ClassFinder, boolean)} instead
     */
    @Deprecated
    public SpaceModule( final ClassSpace space, final BeanScanning scanning )
    {
        this( space, scanning, false );
    }

    public SpaceModule( final ClassSpace space, final BeanScanning scanning, boolean isStrict )
    {
        caching = BeanScanning.CACHE == scanning;
        this.space = space;
        switch ( scanning )
        {
            case OFF:
                finder = null;
                break;
            case INDEX:
                finder = LOCAL_INDEX;
                break;
            case GLOBAL_INDEX:
                finder = GLOBAL_INDEX;
                break;
            default:
                finder = LOCAL_SCAN;
                break;
        }
        this.isStrict = isStrict;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Applies a new visitor {@link Strategy} to the current module.
     * 
     * @param _strategy The new strategy
     * @return Updated module
     */
    public Module with( final Strategy _strategy )
    {
        strategy = _strategy;
        return this;
    }

    public void configure( final Binder binder )
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
    // Public types
    // ----------------------------------------------------------------------

    /**
     * Visitor strategy.
     */
    public interface Strategy
    {
        /**
         * Selects the {@link SpaceVisitor} to be used for the given {@link Binder}.
         * 
         * @param binder The binder
         * @return Selected visitor
         */
        SpaceVisitor visitor( Binder binder );

        /**
         * Default visitor strategy; scan and bind implementations with {@link Qualifier}s.
         */
        Strategy DEFAULT = new Strategy()
        {
            public SpaceVisitor visitor( final Binder binder )
            {
                return new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ) );
            }
        };

        /**
         * Same as {@link #DEFAULT} but throwing {@link RuntimeException} in case class cannot be scanned.
         */
        Strategy DEFAULT_STRICT = new Strategy()
        {
            public SpaceVisitor visitor( final Binder binder )
            {
                return new QualifiedTypeVisitor( new QualifiedTypeBinder( binder ), true );
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    void scanForElements( final Binder binder )
    {
        new SpaceScanner( space, finder, isStrict ).accept( strategy.visitor( binder ) );
    }

    private void recordAndReplayElements( final Binder binder )
    {
        final String key = space.toString();
        List<Element> elements = RecordedElements.cache.get( key );
        if ( null == elements )
        {
            // record results of scanning plus any custom module bindings
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
                // shortcut, no need to reset state first time round
                Elements.getModule( recording ).configure( binder );
                return;
            }
        }

        replayRecordedElements( binder, elements );
    }

    private static void replayRecordedElements( final Binder binder, final List<Element> elements )
    {
        for ( final Element e : elements )
        {
            // lookups have state so we replace them with duplicates when replaying...
            if ( e instanceof ProviderLookup<?> )
            {
                binder.getProvider( ( (ProviderLookup<?>) e ).getKey() );
            }
            else if ( e instanceof MembersInjectorLookup<?> )
            {
                binder.getMembersInjector( ( (MembersInjectorLookup<?>) e ).getType() );
            }
            else if ( e instanceof PrivateElements )
            {
                // Follows example set by Guice Modules when applying private elements:
                final PrivateElements privateElements = (PrivateElements) e;

                // 1. create new private binder, using the elements source token
                final PrivateBinder privateBinder = binder.withSource( e.getSource() ).newPrivateBinder();

                // 2. for all elements, apply each element to the private binder
                replayRecordedElements( privateBinder, privateElements.getElements() );

                // 3. re-expose any exposed keys using their exposed source token
                for ( final Key<?> k : privateElements.getExposedKeys() )
                {
                    privateBinder.withSource( privateElements.getExposedSource( k ) ).expose( k );
                }
            }
            else
            {
                e.applyTo( binder );
            }
        }
    }
}
