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

import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.converters.composite.MapConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class MapOrientedComponentConfigurator
    extends AbstractComponentConfigurator
{
    @Override
    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm,
                                    final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        if ( component instanceof MapOrientedComponent )
        {
            Object map = new MapConverter().fromConfiguration( converterLookup, configuration, Map.class,
                                                               component.getClass(), realm, evaluator, listener );
            ( (MapOrientedComponent) component ).setComponentConfiguration( (Map<?, ?>) map );
        }
        else
        {
            throw new ComponentConfigurationException( "Component does not implement " + MapOrientedComponent.class );
        }
    }
}
