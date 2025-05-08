/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
package org.eclipse.sisu.bean;

import java.lang.reflect.Member;
import java.util.Iterator;

import org.eclipse.sisu.bean.DeclaredMembers.View;

/**
 * {@link Iterable} that iterates over potential bean properties in a class hierarchy.
 */
public final class BeanProperties
    implements Iterable<BeanProperty<Object>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<Member> members;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanProperties( final Class<?> clazz )
    {
        if ( clazz.isAnnotationPresent( IgnoreSetters.class ) )
        {
            members = new DeclaredMembers( clazz, View.FIELDS );
        }
        else
        {
            members = new DeclaredMembers( clazz, View.METHODS, View.FIELDS );
        }
    }

    BeanProperties( final Iterable<Member> members )
    {
        this.members = members;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterator<BeanProperty<Object>> iterator()
    {
        return new BeanPropertyIterator<Object>( members );
    }
}
