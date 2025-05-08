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
package org.eclipse.sisu.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.BindingSubscriber;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.space.GlobberStrategy;
import org.eclipse.sisu.space.Tokens;
import org.osgi.framework.BundleContext;

import com.google.inject.Binding;

/**
 * On-demand publisher of {@link Binding}s from the OSGi service registry.
 */
public final class ServiceBindings
    implements BindingPublisher
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<String, BindingTracker<?>> trackers =
        new ConcurrentHashMap<String, BindingTracker<?>>( 16, 0.75f, 1 );

    private final BundleContext context;

    private final String[] allowed;

    private final String[] ignored;

    private final int maxRank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates new publisher of service bindings, using the given OSGi {@link BundleContext} to track services.<br>
     * <br>
     * The globbed patterns control whether tracking requests for particular types are allowed or ignored.<br>
     * Any published bindings are ranked according to their service ranking (up to the given maximum).
     * 
     * @param context The tracking context
     * @param allow Globbed pattern of packages/types to allow
     * @param ignore Globbed pattern of packages/types to ignore
     * @param maxRank Maximum binding rank
     */
    public ServiceBindings( final BundleContext context, final String allow, final String ignore, final int maxRank )
    {
        this.context = context;
        this.maxRank = maxRank;

        allowed = parseGlobs( allow );
        ignored = parseGlobs( ignore );
    }

    /**
     * Creates new publisher of service bindings, using the given OSGi {@link BundleContext} to track services.<br>
     * <br>
     * Uses default allow/ignore settings and assigns any published services the lowest possible ranking.
     * 
     * @param context The tracking context
     */
    public ServiceBindings( final BundleContext context )
    {
        this( context, defaultAllow(), defaultIgnore(), Integer.MIN_VALUE );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Configured by {@code org.eclipse.sisu.osgi.ServiceBindings.allow} system property.
     * @return Globbed pattern of types to allow
     */
    public static String defaultAllow()
    {
        return System.getProperty( ServiceBindings.class.getName() + ".allow", "" );
    }

    /**
     * Configured by {@code org.eclipse.sisu.osgi.ServiceBindings.ignore} system property.
     * @return Globbed pattern of types to ignore
     */
    public static String defaultIgnore()
    {
        return System.getProperty( ServiceBindings.class.getName() + ".ignore", "" );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public <T> void subscribe( final BindingSubscriber<T> subscriber )
    {
        final String clazzName = subscriber.type().getRawType().getName();
        if ( shouldTrack( clazzName ) )
        {
            BindingTracker tracker = trackers.get( clazzName );
            if ( null == tracker )
            {
                tracker = new BindingTracker<T>( context, maxRank, clazzName );
                final BindingTracker oldTracker = trackers.putIfAbsent( clazzName, tracker );
                if ( null != oldTracker )
                {
                    tracker = oldTracker; // someone got there first, use their tracker
                }
            }
            tracker.subscribe( subscriber );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public <T> void unsubscribe( final BindingSubscriber<T> subscriber )
    {
        final String clazzName = subscriber.type().getRawType().getName();
        final BindingTracker tracker = trackers.get( clazzName );
        if ( null != tracker )
        {
            tracker.unsubscribe( subscriber );
        }
    }

    public int maxBindingRank()
    {
        return maxRank;
    }

    public <T> T adapt( final Class<T> type )
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean shouldTrack( final String clazzName )
    {
        for ( final String allow : allowed )
        {
            if ( GlobberStrategy.PATTERN.matches( allow, clazzName ) )
            {
                for ( final String ignore : ignored )
                {
                    if ( GlobberStrategy.PATTERN.matches( ignore, clazzName ) )
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static String[] parseGlobs( final String globs )
    {
        final List<String> patterns = new ArrayList<String>();
        for ( final String glob : Tokens.splitByComma( globs ) )
        {
            patterns.add( GlobberStrategy.PATTERN.compile( glob ) );
        }
        return patterns.toArray( new String[patterns.size()] );
    }
}
