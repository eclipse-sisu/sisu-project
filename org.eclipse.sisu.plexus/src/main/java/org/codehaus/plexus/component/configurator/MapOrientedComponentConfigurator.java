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
package org.codehaus.plexus.component.configurator;

import java.util.Map;
import javax.inject.Named;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.converters.composite.MapConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Named("map-oriented")
public class MapOrientedComponentConfigurator extends AbstractComponentConfigurator {
    @Override
    public void configureComponent(
            final Object component,
            final PlexusConfiguration configuration,
            final ExpressionEvaluator evaluator,
            final ClassRealm realm,
            final ConfigurationListener listener)
            throws ComponentConfigurationException {
        if (component instanceof MapOrientedComponent) {
            Object map = new MapConverter()
                    .fromConfiguration(
                            converterLookup,
                            configuration,
                            Map.class,
                            component.getClass(),
                            realm,
                            evaluator,
                            listener);
            ((MapOrientedComponent) component).setComponentConfiguration((Map<?, ?>) map);
        } else {
            throw new ComponentConfigurationException("Component does not implement " + MapOrientedComponent.class);
        }
    }
}
