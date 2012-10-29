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
package org.codehaus.plexus.component.configurator.converters.special;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public final class ClassRealmConverter
    extends AbstractConfigurationConverter
{
    private final ClassRealm realm;

    public ClassRealmConverter( final ClassRealm realm )
    {
        this.realm = realm;
    }

    public boolean canConvert( final Class<?> type )
    {
        return ClassRealm.class.isAssignableFrom( type )
            || org.codehaus.classworlds.ClassRealm.class.isAssignableFrom( type );
    }

    @Override
    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> contextType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object result = fromExpression( configuration, evaluator, type );
        if ( null == result )
        {
            result = realm;
        }
        if ( !ClassRealm.class.isAssignableFrom( type ) && result instanceof ClassRealm )
        {
            result = ClassRealmAdapter.getInstance( (ClassRealm) result );
        }
        return result;
    }
}
