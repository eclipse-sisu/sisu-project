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
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.RequireExplicitBindingsOption;
import com.google.inject.spi.StaticInjectionRequest;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.Guice4;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.inject.TypeArguments;
import org.eclipse.sisu.wire.WireModule.Strategy;

/**
 * {@link ElementVisitor} that analyzes {@link Binding}s for unresolved injection dependencies.
 */
final class ElementAnalyzer extends DefaultElementVisitor<Void> {
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static {
        final Map<Key<?>, Key<?>> aliases = new HashMap<>();
        try {
            addLegacyKeyAlias(aliases, BeanLocator.class);
            addLegacyKeyAlias(aliases, MutableBeanLocator.class);
            addLegacyKeyAlias(aliases, RankingFunction.class);
        } catch (final LinkageError | Exception e) // NOSONAR
        {
            // legacy wrappers are not available
        }
        LEGACY_KEY_ALIASES = aliases.isEmpty() ? null : aliases;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Map<Key<?>, Key<?>> LEGACY_KEY_ALIASES;

    private static final List<Element> JIT_BINDINGS = Elements.getElements(new Module() {
        @Override
        public void configure(final Binder binder) {
            binder.bind(BeanLocator.class).to(MutableBeanLocator.class);
            binder.bind(MutableBeanLocator.class).to(DefaultBeanLocator.class);
            binder.bind(TypeConverterCache.class);
        }
    });

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Key<?>> localKeys = new HashSet<>();

    private final DependencyAnalyzer analyzer = new DependencyAnalyzer();

    private final List<ElementAnalyzer> privateAnalyzers = new ArrayList<>();

    private final List<Map<?, ?>> properties = new ArrayList<>();

    private final List<String> arguments = new ArrayList<>();

    private final Binder binder;

    private boolean requireExplicitBindings;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ElementAnalyzer(final Binder binder) {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void ignoreKeys(final Set<Key<?>> keys) {
        localKeys.addAll(keys);
    }

    public void apply(final Strategy strategy) {
        if (requireExplicitBindings) {
            makeJitBindingsExplicit();
        }

        // calculate which dependencies are missing from the module elements
        final Set<Key<?>> missingKeys = analyzer.findMissingKeys(localKeys);
        final Map<?, ?> mergedProperties = new MergedProperties(properties);

        final Wiring wiring = strategy.wiring(binder);
        for (final Key<?> key : missingKeys) {
            if (isParameters(key)) {
                wireParameters(key, mergedProperties);
            } else if (!isRestricted(key)) {
                wiring.wire(key);
            }
        }

        for (final ElementAnalyzer privateAnalyzer : privateAnalyzers) {
            // ignore parent local/wired dependencies
            privateAnalyzer.ignoreKeys(localKeys);
            privateAnalyzer.ignoreKeys(missingKeys);
            privateAnalyzer.apply(strategy);
        }
    }

    @Override
    public <T> Void visit(final Binding<T> binding) {
        final Key<T> key = binding.getKey();
        if (!localKeys.contains(key)) {
            if (isParameters(key)) {
                mergeParameters(binding);
            } else if (Boolean.TRUE.equals(binding.acceptTargetVisitor(analyzer))) {
                localKeys.add(key);
                binding.applyTo(binder);

                if (null != LEGACY_KEY_ALIASES) {
                    @SuppressWarnings("unchecked")
                    final Key<T> alias = (Key<T>) LEGACY_KEY_ALIASES.get(key);
                    if (null != alias && localKeys.add(alias)) {
                        binder.bind(alias).to(key); // chain to legacy binding
                    }
                }
            } else {
                Logs.trace("Discard binding: {}", binding, null);
            }
        }
        return null;
    }

    @Override
    public Void visit(final PrivateElements elements) {
        // Follows example set by Guice Modules when rewriting private elements:
        //
        // 1. create new private binder, using the elements source token
        // 2. for all elements, apply each element to the private binder
        // 3. re-expose any exposed keys using their exposed source token

        final PrivateBinder privateBinder =
                binder.withSource(elements.getSource()).newPrivateBinder();
        final ElementAnalyzer privateAnalyzer = new ElementAnalyzer(privateBinder);

        privateAnalyzers.add(privateAnalyzer);

        // ignore bindings already in the parent
        privateAnalyzer.ignoreKeys(localKeys);
        for (final Element e : elements.getElements()) {
            e.acceptVisitor(privateAnalyzer);
        }

        for (final Key<?> k : elements.getExposedKeys()) {
            // only expose valid bindings that won't conflict with existing ones
            if (privateAnalyzer.localKeys.contains(k) && localKeys.add(k)) {
                privateBinder.withSource(elements.getExposedSource(k)).expose(k);
            }
        }

        return null;
    }

    @Override
    public <T> Void visit(final ProviderLookup<T> lookup) {
        analyzer.visit(lookup);
        lookup.applyTo(binder);
        return null;
    }

    @Override
    public Void visit(final StaticInjectionRequest request) {
        analyzer.visit(request);
        request.applyTo(binder);
        return null;
    }

    @Override
    public Void visit(final InjectionRequest<?> request) {
        analyzer.visit(request);
        request.applyTo(binder);
        return null;
    }

    @Override
    public Void visit(final RequireExplicitBindingsOption option) {
        requireExplicitBindings = true;
        option.applyTo(binder);
        return null;
    }

    @Override
    public Void visitOther(final Element element) {
        element.applyTo(binder);
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void makeJitBindingsExplicit() {
        for (final Element element : JIT_BINDINGS) {
            if (element instanceof Binding<?> && localKeys.add(((Binding<?>) element).getKey())) {
                element.applyTo(binder);
            }
        }
    }

    private void mergeParameters(final Binding<?> binding) {
        Object parameters = Guice4.invokeStaticBinding(binding);
        if (parameters instanceof Map) {
            properties.add((Map<?, ?>) parameters);
        } else if (parameters instanceof String[]) {
            Collections.addAll(arguments, (String[]) parameters);
        } else {
            Logs.warn("Ignoring incompatible @Parameters binding: {}", binding, null);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void wireParameters(final Key key, final Map mergedProperties) {
        if (ParameterKeys.PROPERTIES.equals(key)) {
            binder.bind(key).toInstance(mergedProperties);
        } else {
            final TypeLiteral<?> type = key.getTypeLiteral();
            final Class<?> clazz = type.getRawType();
            if (Map.class == clazz) {
                final TypeLiteral<?>[] constraints = TypeArguments.get(type);
                if (constraints.length == 2 && String.class == constraints[1].getRawType()) {
                    binder.bind(key).to(StringProperties.class);
                } else {
                    binder.bind(key).to(ParameterKeys.PROPERTIES);
                }
            } else if (String[].class == clazz) {
                binder.bind(key).toInstance(arguments.toArray(new String[arguments.size()]));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isParameters(final Key<?> key) {
        final Class<? extends Annotation> qualifierType = key.getAnnotationType();
        return Parameters.class == qualifierType || org.sonatype.inject.Parameters.class == qualifierType;
    }

    private static boolean isRestricted(final Key<?> key) {
        final String name = key.getTypeLiteral().getRawType().getName();
        if (name.startsWith("org.eclipse.sisu.inject") || name.startsWith("org.sonatype.guice.bean.locators")) {
            return name.endsWith("BeanLocator")
                    || name.endsWith("BindingPublisher")
                    || name.endsWith("RankingFunction");
        }
        return "org.slf4j.Logger".equals(name);
    }

    private static void addLegacyKeyAlias(final Map<Key<?>, Key<?>> aliases, final Class<?> clazz)
            throws ClassNotFoundException {
        final String legacyName = "org.sonatype.guice.bean.locators." + clazz.getSimpleName();
        final Class<?> legacyType = ElementAnalyzer.class.getClassLoader().loadClass(legacyName);
        if (clazz.isAssignableFrom(legacyType)) {
            aliases.put(Key.get(legacyType), Key.get(clazz));
        }
    }
}
