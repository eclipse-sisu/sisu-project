/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.BindingSubscriber;
import org.eclipse.sisu.inject.Logs;
import org.osgi.framework.BundleContext;

import com.google.inject.Binding;

/**
 * On-demand publisher of {@link Binding}s from the OSGi service registry.
 */
public final class ServiceBindings
    implements BindingPublisher
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Pattern GLOB_SYNTAX = Pattern.compile( "(?:\\w+|\\*)(?:\\.?(?:\\w+|\\*))*" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<String, BindingTracker<?>> trackers =
        new ConcurrentHashMap<String, BindingTracker<?>>();

    private final BundleContext context;

    private final Pattern[] allowed;

    private final Pattern[] ignored;

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
     * @return Globbed pattern of types to allow
     * @see {@code org.eclipse.sisu.osgi.ServiceBindings.allow} system property
     */
    public static String defaultAllow()
    {
        return System.getProperty( ServiceBindings.class.getName() + ".allow", "" );
    }

    /**
     * @return Globbed pattern of types to ignore
     * @see {@code org.eclipse.sisu.osgi.ServiceBindings.ignore} system property
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean shouldTrack( final String clazzName )
    {
        for ( final Pattern allow : allowed )
        {
            if ( allow.matcher( clazzName ).matches() )
            {
                for ( final Pattern ignore : ignored )
                {
                    if ( ignore.matcher( clazzName ).matches() )
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static Pattern[] parseGlobs( final String globs )
    {
        final List<Pattern> patterns = new ArrayList<Pattern>();
        for ( final String glob : globs.split( "\\s*,\\s*" ) )
        {
            if ( GLOB_SYNTAX.matcher( glob ).matches() )
            {
                patterns.add( Pattern.compile( glob.replace( ".", "\\." ).replace( "*", ".*" ) ) );
            }
            else if ( glob.length() > 0 )
            {
                Logs.warn( "Ignoring malformed glob pattern: {}", glob, null );
            }
        }
        return patterns.toArray( new Pattern[patterns.size()] );
    }
}
