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
package org.eclipse.sisu.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Read-only {@link Iterator} that picks out potential bean properties from declared members.
 */
final class BeanPropertyIterator<T>
    implements Iterator<BeanProperty<T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterator<Member> memberIterator;

    // look-ahead, maintained by hasNext()
    private BeanProperty<T> nextProperty;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    BeanPropertyIterator( final Iterable<Member> members )
    {
        memberIterator = members.iterator();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean hasNext()
    {
        while ( null == nextProperty )
        {
            if ( !memberIterator.hasNext() )
            {
                return false; // no more properties
            }

            final Member member = memberIterator.next();
            final int modifiers = member.getModifiers();

            // static members can't be properties, abstracts and synthetics are just noise so we ignore them
            if ( Modifier.isStatic( modifiers ) || Modifier.isAbstract( modifiers ) || member.isSynthetic() )
            {
                continue;
            }

            if ( member instanceof Method )
            {
                final Method method = (Method) member;
                if ( isSetter( method ) )
                {
                    nextProperty = new BeanPropertySetter<T>( method );
                }
            }
            else if ( member instanceof Field )
            {
                nextProperty = new BeanPropertyField<T>( (Field) member );
            }

            // ignore properties annotated with JSR330/Guice
            if ( null != nextProperty && atInject( member ) )
            {
                nextProperty = null;
            }
        }

        return true;
    }

    public BeanProperty<T> next()
    {
        if ( hasNext() )
        {
            // initialized by hasNext()
            final BeanProperty<T> property = nextProperty;
            nextProperty = null;
            return property;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static boolean isSetter( final Method method )
    {
        final String name = method.getName();
        if ( name.startsWith( "set" ) && name.length() > 3 && Character.isUpperCase( name.charAt( 3 ) ) )
        {
            return method.getParameterTypes().length == 1;
        }
        return false;
    }

    private static boolean atInject( final Member member )
    {
        final AnnotatedElement e = (AnnotatedElement) member;
        return e.isAnnotationPresent( javax.inject.Inject.class )
            || e.isAnnotationPresent( com.google.inject.Inject.class );
    }
}
