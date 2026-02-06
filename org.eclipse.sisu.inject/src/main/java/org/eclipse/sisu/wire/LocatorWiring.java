/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.eclipse.sisu.wire;

import com.google.inject.Binder;
import com.google.inject.ImplementedBy;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Qualifier;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Dynamic;
import org.eclipse.sisu.Hidden;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.Sources;
import org.eclipse.sisu.inject.TypeArguments;

/**
 * Adds {@link BeanLocator}-backed bindings for unresolved bean dependencies.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class LocatorWiring implements Wiring {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Hidden HIDDEN_WIRING = Sources.hide(LocatorWiring.class.getName());

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanProviders beanProviders;

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public LocatorWiring(final Binder binder) {
        beanProviders = new BeanProviders(binder);
        this.binder = binder.withSource(HIDDEN_WIRING);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public boolean wire(final Key<?> key) {
        final Class<?> clazz = key.getTypeLiteral().getRawType();
        if (Map.class == clazz) {
            bindMapImport(key);
        } else if (List.class == clazz || Collection.class == clazz || Iterable.class == clazz) {
            bindListImport(key);
        } else if (Set.class == clazz) {
            bindSetImport(key);
        } else {
            bindBeanImport(key);
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Adds an imported {@link Map} binding; uses the generic type arguments to determine the search details.
     *
     * @param key The dependency key
     */
    private void bindMapImport(final Key key) {
        final TypeLiteral<?>[] args = TypeArguments.get(key.getTypeLiteral());
        if (2 == args.length && null == key.getAnnotation()) {
            final Class qualifierType = args[0].getRawType();
            if (String.class == qualifierType) {
                binder.bind(key).toProvider(beanProviders.stringMapOf(args[1]));
            } else if (qualifierType.isAnnotation()) {
                binder.bind(key).toProvider(beanProviders.mapOf(Key.get(args[1], qualifierType)));
            } else if (Annotation.class == qualifierType) {
                binder.bind(key).toProvider(beanProviders.mapOf(Key.get(args[1])));
            }
        }
    }

    /**
     * Adds an imported {@link List} binding; uses the generic type arguments to determine the search details.
     *
     * @param key The dependency key
     */
    @SuppressWarnings("deprecation")
    private void bindListImport(final Key key) {
        final TypeLiteral<?>[] args = TypeArguments.get(key.getTypeLiteral());
        if (1 == args.length && null == key.getAnnotation()) {
            final TypeLiteral<?> elementType = args[0];
            if (BeanEntry.class == elementType.getRawType()
                    || org.sonatype.inject.BeanEntry.class == elementType.getRawType()) {
                final Provider beanEntriesProvider = getBeanEntriesProvider(elementType);
                if (null != beanEntriesProvider) {
                    binder.bind(key).toProvider(beanEntriesProvider);
                }
            } else {
                binder.bind(key).toProvider(beanProviders.listOf(Key.get(elementType)));
            }
        }
    }

    /**
     * Returns the appropriate {@link BeanEntry} provider for the given entry type.
     *
     * @param entryType The entry type
     * @return Provider of bean entries
     */
    @SuppressWarnings("deprecation")
    private Provider getBeanEntriesProvider(final TypeLiteral entryType) {
        final TypeLiteral<?>[] args = TypeArguments.get(entryType);
        if (2 == args.length) {
            final Class qualifierType = args[0].getRawType();
            final Key key = qualifierType.isAnnotation() ? Key.get(args[1], qualifierType) : Key.get(args[1]);
            final Provider beanEntries = beanProviders.beanEntriesOf(key);

            return BeanEntry.class == entryType.getRawType()
                    ? beanEntries
                    : org.eclipse.sisu.inject.Legacy.adapt(beanEntries);
        }
        return null;
    }

    /**
     * Adds an imported {@link Set} binding; uses the generic type arguments to determine the search details.
     *
     * @param key The dependency key
     */
    private void bindSetImport(final Key key) {
        final TypeLiteral<?>[] args = TypeArguments.get(key.getTypeLiteral());
        if (1 == args.length && null == key.getAnnotation()) {
            binder.bind(key).toProvider(beanProviders.setOf(Key.get(args[0])));
        }
    }

    /**
     * Adds an imported bean binding; uses the type and {@link Qualifier} annotation to determine the search details.
     *
     * @param key The dependency key
     */
    private <T> void bindBeanImport(final Key<T> key) {
        final Annotation qualifier = key.getAnnotation();
        if (qualifier instanceof Named) {
            if (((Named) qualifier).value().length() == 0) {
                // special case for wildcard @Named dependencies: match any @Named bean regardless of actual name
                binder.bind(key).toProvider(beanProviders.firstOf(Key.get(key.getTypeLiteral(), Named.class)));
            } else {
                binder.bind(key).toProvider(beanProviders.placeholderOf(key));
            }
        } else if (qualifier instanceof Dynamic) {
            final Provider<T> delegate = beanProviders.firstOf(Key.get(key.getTypeLiteral()));
            binder.bind(key).toInstance(GlueLoader.dynamicGlue(key.getTypeLiteral(), delegate));
        } else {
            binder.bind(key).toProvider(beanProviders.firstOf(key));

            // capture original implicit binding?
            if (null == key.getAnnotationType()) {
                bindImplicitType(key.getTypeLiteral());
            }
        }
    }

    /**
     * Captures the original implicit binding that would have been used by Guice; see the {@link BeanLocator} code.
     *
     * @param type The implicit type
     */
    private void bindImplicitType(final TypeLiteral type) {
        try {
            final Class<?> clazz = type.getRawType();
            if (TypeArguments.isConcrete(clazz)) {
                final Member ctor = InjectionPoint.forConstructorOf(type).getMember();
                binder.bind(TypeArguments.implicitKey(clazz)).toConstructor((Constructor) ctor);
            } else {
                final ImplementedBy implementedBy = clazz.getAnnotation(ImplementedBy.class);
                if (null != implementedBy) {
                    binder.bind(TypeArguments.implicitKey(clazz)).to((Class) implementedBy.value());
                } else {
                    final ProvidedBy providedBy = clazz.getAnnotation(ProvidedBy.class);
                    if (null != providedBy) {
                        binder.bind(TypeArguments.implicitKey(clazz)).toProvider((Class) providedBy.value());
                    }
                }
            }
        } catch (final LinkageError | RuntimeException e) // NOSONAR
        {
            // can safely ignore
        }
    }
}
