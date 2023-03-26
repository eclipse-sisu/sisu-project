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
package org.codehaus.plexus.component.configurator.converters.special;

import java.util.ArrayDeque;
import java.util.Deque;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.classworlds.ClassRealmReverseAdapter;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@SuppressWarnings( { "unchecked", "deprecation" } )
public final class ClassRealmConverter
    extends AbstractConfigurationConverter
{
    private static ThreadLocal<Object> context = new ThreadLocal<Object>();

    private final ClassRealm realm;

    public ClassRealmConverter( final ClassRealm realm )
    {
        this.realm = realm;
    }

    public ClassRealmConverter( final org.codehaus.classworlds.ClassRealm realm )
    {
        this.realm = ClassRealmReverseAdapter.getInstance( realm );
    }

    public ClassRealmConverter()
    {
        this.realm = null;
    }

    public static void pushContextRealm( final ClassRealm realm )
    {
        final Object holder = context.get();
        if ( null == holder )
        {
            context.set( realm );
        }
        else if ( holder instanceof ClassRealm )
        {
            // upgrade from single realm to stack of realms
            final Deque<ClassRealm> stack = new ArrayDeque<ClassRealm>();
            stack.add( realm );
            stack.add( (ClassRealm) holder );
            context.set( stack );
        }
        else if ( holder instanceof Deque<?> )
        {
            ( (Deque<ClassRealm>) holder ).addFirst( realm );
        }
    }

    public static void popContextRealm()
    {
        final Object holder = context.get();
        if ( holder instanceof ClassRealm )
        {
            context.remove();
        }
        else if ( holder instanceof Deque<?> )
        {
            final Deque<ClassRealm> stack = (Deque<ClassRealm>) holder;
            if ( stack.size() == 2 )
            {
                // downgrade to single realm
                context.set( stack.peekLast() );
            }
            else
            {
                stack.removeFirst();
            }
        }
    }

    public ClassRealm peekContextRealm()
    {
        final Object holder = context.get();
        if ( holder instanceof ClassRealm )
        {
            return (ClassRealm) holder;
        }
        else if ( holder instanceof Deque<?> )
        {
            return ( (Deque<ClassRealm>) holder ).getFirst();
        }
        return realm;
    }

    public boolean canConvert( final Class<?> type )
    {
        return ClassRealm.class.isAssignableFrom( type )
            || org.codehaus.classworlds.ClassRealm.class.isAssignableFrom( type );
    }

    public Object fromConfiguration( final ConverterLookup lookup, final PlexusConfiguration configuration,
                                     final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                     final ExpressionEvaluator evaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object result = fromExpression( configuration, evaluator, type );
        if ( null == result )
        {
            result = peekContextRealm();
        }
        if ( !ClassRealm.class.isAssignableFrom( type ) && result instanceof ClassRealm )
        {
            result = ClassRealmAdapter.getInstance( (ClassRealm) result );
        }
        return result;
    }
}
