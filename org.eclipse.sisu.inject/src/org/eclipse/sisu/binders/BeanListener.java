/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.binders;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.locators.BeanLocator;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link InjectionListener} that listens for mediated watchers and registers them with the {@link BeanLocator}.
 */
final class BeanListener
    implements TypeListener, InjectionListener<Object>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Mediation<?, ?, ?>> mediation = new ArrayList<Mediation<?, ?, ?>>();

    @Inject
    private BeanLocator locator;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Adds a {@link Mediation} record containing the necessary details about a mediated watcher.
     * 
     * @param key The watched key
     * @param mediator The bean mediator
     * @param watcherType The watcher type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void mediate( final Key key, final Mediator mediator, final Class watcherType )
    {
        mediation.add( new Mediation( key, mediator, watcherType ) );
    }

    public <T> void hear( final TypeLiteral<T> type, final TypeEncounter<T> encounter )
    {
        for ( final Mediation<?, ?, ?> m : mediation )
        {
            if ( m.watcherType.isAssignableFrom( type.getRawType() ) )
            {
                encounter.register( this ); // look out for watcher instances
            }
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void afterInjection( final Object watcher )
    {
        for ( final Mediation m : mediation )
        {
            if ( m.watcherType.isInstance( watcher ) )
            {
                locator.watch( m.watchedKey, m.mediator, watcher );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Record containing all the necessary details about a mediated watcher.
     */
    private static final class Mediation<Q extends Annotation, T, W>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        final Key<T> watchedKey;

        final Mediator<Q, T, W> mediator;

        final Class<W> watcherType;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        Mediation( final Key<T> watchedKey, final Mediator<Q, T, W> mediator, final Class<W> watcherType )
        {
            this.watchedKey = watchedKey;
            this.mediator = mediator;
            this.watcherType = watcherType;
        }
    }
}
