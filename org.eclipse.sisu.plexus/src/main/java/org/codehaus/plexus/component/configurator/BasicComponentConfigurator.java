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

import javax.inject.Named;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Named("basic")
public class BasicComponentConfigurator extends AbstractComponentConfigurator {
    @Override
    public void configureComponent(
            final Object component,
            final PlexusConfiguration configuration,
            final ExpressionEvaluator evaluator,
            final ClassRealm realm,
            final ConfigurationListener listener)
            throws ComponentConfigurationException {
        try {
            ClassRealmConverter.pushContextRealm(realm);

            new ObjectWithFieldsConverter()
                    .processConfiguration(
                            converterLookup,
                            component,
                            realm, //
                            configuration,
                            evaluator,
                            listener);
        } finally {
            ClassRealmConverter.popContextRealm();
        }
    }
}
