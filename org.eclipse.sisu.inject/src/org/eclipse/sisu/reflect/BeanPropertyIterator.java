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
                if ( isSetterOrAdder( member ) )
                {
                    nextProperty = new BeanPropertySetter<T>( (Method) member );
                }
            }
            else if ( member instanceof Field )
            {
                nextProperty = new BeanPropertyField<T>( (Field) member );
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

    private static boolean isSetterOrAdder( final Member member )
    {
        final String name = member.getName();
        return ( name.startsWith( "set" ) || name.startsWith( "add" ) ) //
            && name.length() > 3
            && Character.isUpperCase( name.charAt( 3 ) )
            && ( (Method) member ).getParameterTypes().length == 1;
    }
}
