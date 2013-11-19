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
package org.eclipse.sisu.wire;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.TypeArguments;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

// ----------------------------------------------------------------------
// BeanLocator-backed Providers that can provide dynamic bean lookups
// ----------------------------------------------------------------------

/**
 * Provides an {@link Iterable} sequence of {@link BeanEntry}s.
 */
final class BeanEntryProvider<K extends Annotation, V>
    implements Provider<Iterable<? extends BeanEntry<K, V>>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<BeanLocator> locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanEntryProvider( final Provider<BeanLocator> locator, final Key<V> key )
    {
        this.locator = locator;
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<? extends BeanEntry<K, V>> get()
    {
        return locator.get().locate( key );
    }
}

// ----------------------------------------------------------------------

/**
 * Base class for {@link Collection}s of qualified beans.
 */
class AbstractBeans<K extends Annotation, V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<BeanLocator> locator;

    private final Key<?> key;

    private final boolean isProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    AbstractBeans( final Provider<BeanLocator> locator, final Key<V> key )
    {
        this.locator = locator;
        final TypeLiteral<V> type = key.getTypeLiteral();
        final Class<?> clazz = type.getRawType();
        isProvider = javax.inject.Provider.class == clazz || com.google.inject.Provider.class == clazz;
        if ( isProvider )
        {
            this.key = key.ofType( TypeArguments.get( type, 0 ) );
        }
        else
        {
            this.key = key;
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    protected final Iterable<Entry<K, V>> beans()
    {
        final Iterable beans = locator.get().locate( key );
        return isProvider ? new ProviderIterableAdapter( beans ) : beans;
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link List} of qualified beans.
 */
final class BeanListProvider<K extends Annotation, V>
    extends AbstractBeans<K, V>
    implements Provider<List<V>>
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanListProvider( final Provider<BeanLocator> locator, final Key<V> key )
    {
        super( locator, key );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public List<V> get()
    {
        return new EntryListAdapter<K, V>( beans() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Set} of qualified beans.
 */
final class BeanSetProvider<K extends Annotation, V>
    extends AbstractBeans<K, V>
    implements Provider<Set<V>>
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanSetProvider( final Provider<BeanLocator> locator, final Key<V> key )
    {
        super( locator, key );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Set<V> get()
    {
        return new EntrySetAdapter<K, V>( beans() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of qualified beans.
 */
final class BeanMapProvider<K extends Annotation, V>
    extends AbstractBeans<K, V>
    implements Provider<Map<K, V>>
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanMapProvider( final Provider<BeanLocator> locator, final Key<V> key )
    {
        super( locator, key );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<K, V> get()
    {
        return new EntryMapAdapter<K, V>( beans() );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a {@link Map} of named beans.
 */
final class NamedBeanMapProvider<V>
    extends AbstractBeans<Named, V>
    implements Provider<Map<String, V>>
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NamedBeanMapProvider( final Provider<BeanLocator> locator, final TypeLiteral<V> type )
    {
        super( locator, Key.get( type, Named.class ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<String, V> get()
    {
        return new EntryMapAdapter<String, V>( new NamedIterableAdapter<V>( beans() ) );
    }
}

// ----------------------------------------------------------------------

/**
 * Provides a single qualified bean.
 */
final class BeanProvider<V>
    implements Provider<V>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<BeanLocator> locator;

    private final Key<V> key;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanProvider( final Provider<BeanLocator> locator, final Key<V> key )
    {
        this.locator = locator;
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public V get()
    {
        return get( locator, key );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static <T> T get( final Provider<BeanLocator> locator, final Key<T> key )
    {
        final Iterator<? extends Entry<Annotation, T>> i = locator.get().locate( key ).iterator();
        return i.hasNext() ? i.next().getValue() : null; // TODO: dynamic proxy??
    }
}

// ----------------------------------------------------------------------
