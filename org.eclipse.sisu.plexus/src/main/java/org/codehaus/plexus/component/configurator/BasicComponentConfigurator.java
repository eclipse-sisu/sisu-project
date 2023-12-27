/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component.configurator;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.inject.Named;

@Named( "basic" )
public class BasicComponentConfigurator
    extends AbstractComponentConfigurator
{
    @Override
    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm,
                                    final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        try
        {
            ClassRealmConverter.pushContextRealm( realm );

            new ObjectWithFieldsConverter().processConfiguration( converterLookup, component, realm, //
                                                                  configuration, evaluator, listener );
        }
        finally
        {
            ClassRealmConverter.popContextRealm();
        }
    }
}
