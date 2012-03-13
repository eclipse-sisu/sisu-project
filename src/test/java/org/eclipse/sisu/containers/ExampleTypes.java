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
package org.eclipse.sisu.containers;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

// various qualified types for testing

interface Foo
{
}

@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@interface Tag
{
    String value();
}

@Named
class NamedFoo
    implements Foo
{
}

@Tag( "A" )
class TaggedFoo
    implements Foo
{
}

@Tag( "B" )
@Named( "NameTag" )
class NamedAndTaggedFoo
    implements Foo
{
}

@Named
@Singleton
class DefaultFoo
    implements Foo
{
}

class TagImpl
    implements Tag
{
    private final String value;

    public TagImpl( final String value )
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return 127 * "value".hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof Tag )
        {
            return value.equals( ( (Tag) rhs ).value() );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "@" + Tag.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType()
    {
        return Tag.class;
    }
}
