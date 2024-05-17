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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.converters.lookup.DefaultConverterLookup;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@SuppressWarnings( "deprecation" )
public abstract class AbstractComponentConfigurator
    implements ComponentConfigurator
{
    private static final ExpressionEvaluator DEFAULT_EXPRESSION_EVALUATOR = new DefaultExpressionEvaluator();

    protected ConverterLookup converterLookup = new DefaultConverterLookup();

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ClassRealm realm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, DEFAULT_EXPRESSION_EVALUATOR, realm );
    }

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm )
        throws ComponentConfigurationException
    {
        configureComponent( component, configuration, evaluator, realm, null );
    }

    public void configureComponent( final Object component, final PlexusConfiguration configuration,
                                    final ExpressionEvaluator evaluator, final ClassRealm realm,
                                    final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        final org.codehaus.classworlds.ClassRealm legacyRealm = ClassRealmAdapter.getInstance( realm );
        final Class<?> clazz = getClass();
        try
        {
            Method configureMethod;
            try
            {
                configureMethod = clazz.getMethod( "configureComponent", Object.class, PlexusConfiguration.class,
                                                   ExpressionEvaluator.class, org.codehaus.classworlds.ClassRealm.class,
                                                   ConfigurationListener.class );
                configureMethod.invoke( this, component, configuration, evaluator, legacyRealm, listener );
            }
            catch ( final NoSuchMethodException ignore )
            {
                configureMethod =
                    clazz.getMethod( "configureComponent", Object.class, PlexusConfiguration.class,
                                     ExpressionEvaluator.class, org.codehaus.classworlds.ClassRealm.class );
                configureMethod.invoke( this, component, configuration, evaluator, legacyRealm );
            }
        }
        catch ( final InvocationTargetException e )
        {
            final Throwable cause = e.getCause();
            if ( cause instanceof ComponentConfigurationException )
            {
                throw (ComponentConfigurationException) cause;
            }
            if ( cause instanceof RuntimeException )
            {
                throw (RuntimeException) cause;
            }
            if ( cause instanceof Error )
            {
                throw (Error) cause;
            }
            throw new ComponentConfigurationException( "Incompatible configurator " + clazz.getName(), cause );
        }
        catch ( final Exception e )
        {
            throw new ComponentConfigurationException( "Incompatible configurator " + clazz.getName(), e );
        }
    }
}
