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

    private final Pattern[] includes;

    private final Pattern[] excludes;

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
     * @param includes Globbed pattern of packages/types to allow
     * @param excludes Globbed pattern of packages/types to ignore
     * @param maxRank Maximum binding rank
     */
    public ServiceBindings( final BundleContext context, final String includes, final String excludes, final int maxRank )
    {
        this.context = context;
        this.includes = parseGlobs( includes );
        this.excludes = parseGlobs( excludes );
        this.maxRank = maxRank;
    }

    /**
     * Creates new publisher of service bindings, using the given OSGi {@link BundleContext} to track services.<br>
     * <br>
     * Uses the default includes/excludes and assigns any published services the lowest possible ranking.
     * 
     * @param context The tracking context
     */
    public ServiceBindings( final BundleContext context )
    {
        this( context, defaultIncludes(), defaultExcludes(), Integer.MIN_VALUE );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * @return Globbed pattern of types to allow
     * @see {@code org.eclipse.sisu.osgi.ServiceBindings.includes} system property
     */
    public static String defaultIncludes()
    {
        return System.getProperty( ServiceBindings.class.getName() + ".includes", "" );
    }

    /**
     * @return Globbed pattern of types to ignore
     * @see {@code org.eclipse.sisu.osgi.ServiceBindings.excludes} system property
     */
    public static String defaultExcludes()
    {
        return System.getProperty( ServiceBindings.class.getName() + ".excludes", "" );
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
        for ( final Pattern include : includes )
        {
            if ( include.matcher( clazzName ).matches() )
            {
                for ( final Pattern exclude : excludes )
                {
                    if ( exclude.matcher( clazzName ).matches() )
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
