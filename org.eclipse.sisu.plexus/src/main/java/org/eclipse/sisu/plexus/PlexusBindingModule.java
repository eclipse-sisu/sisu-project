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
package org.eclipse.sisu.plexus;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.sisu.bean.BeanListener;
import org.eclipse.sisu.bean.BeanManager;

/**
 * Guice {@link Module} that supports registration, injection, and management of Plexus beans.
 */
public final class PlexusBindingModule implements Module {
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanManager manager;

    private final PlexusBeanModule[] modules;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule(final BeanManager manager, final PlexusBeanModule... modules) {
        this.manager = manager;
        this.modules = modules.clone();
    }

    public PlexusBindingModule(final BeanManager manager, final Collection<? extends PlexusBeanModule> modules) {
        this.manager = manager;
        this.modules = modules.toArray(new PlexusBeanModule[modules.size()]);
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void configure(final Binder binder) {
        final List<PlexusBeanSource> sources = new ArrayList<>(modules.length);
        for (final PlexusBeanModule module : modules) {
            final PlexusBeanSource source = module.configure(binder);
            if (null != source) {
                sources.add(source);
            }
        }

        // attach custom logic to support Plexus requirements/configuration/lifecycle
        final PlexusBeanBinder plexusBinder = new PlexusBeanBinder(manager, sources);
        binder.bindListener(Matchers.any(), new BeanListener(plexusBinder));
        if (manager instanceof Module) {
            ((Module) manager).configure(binder);
        }
    }
}
