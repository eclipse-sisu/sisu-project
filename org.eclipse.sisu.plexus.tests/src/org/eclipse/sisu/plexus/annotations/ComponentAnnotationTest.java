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
package org.eclipse.sisu.plexus.annotations;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.plexus.annotations.ComponentImpl;
import org.eclipse.sisu.plexus.config.Strategies;

public class ComponentAnnotationTest
    extends TestCase
{
    interface A
    {
    }

    @Component( role = A.class )
    static class DefaultA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named" )
    static class NamedA
        implements A
    {
    }

    @Component( role = A.class, description = "Something" )
    static class DescribedA
        implements A
    {
    }

    @Component( role = A.class, instantiationStrategy = Strategies.PER_LOOKUP )
    static class PrototypeA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named", instantiationStrategy = Strategies.PER_LOOKUP )
    static class NamedPrototypeA
        implements A
    {
    }

    @Component( role = Simple.class )
    static class Simple
    {
    }

    @Component( role = Simple.class, version = "2" )
    static class Simple2
        extends Simple
    {
    }

    @Component( role = Simple.class, isolatedRealm = true )
    static class Simple3
        extends Simple
    {
    }

    public void testComponentImpl()
        throws ClassNotFoundException
    {
        checkBehaviour( "DefaultA" );
        checkBehaviour( "NamedA" );
        checkBehaviour( "PrototypeA" );
        checkBehaviour( "NamedPrototypeA" );
        checkBehaviour( "DescribedA" );

        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "NamedA" ) ) );
        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "PrototypeA" ) ) );
        assertFalse( replicate( getComponent( "DefaultA" ) ).equals( getComponent( "DescribedA" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "DefaultA" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "Simple2" ) ) );
        assertFalse( replicate( getComponent( "Simple" ) ).equals( getComponent( "Simple3" ) ) );
    }

    private static void checkBehaviour( final String name )
        throws ClassNotFoundException
    {
        final Component orig = getComponent( name );
        final Component clone = replicate( orig );

        assertTrue( orig.equals( clone ) );
        assertTrue( clone.equals( orig ) );
        assertTrue( clone.equals( clone ) );
        assertFalse( clone.equals( "" ) );

        assertEquals( orig.hashCode(), clone.hashCode() );

        assertEquals( new HashSet<String>( Arrays.asList( orig.toString().split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( clone.toString().split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Component getComponent( final String name )
        throws ClassNotFoundException
    {
        return Class.forName( ComponentAnnotationTest.class.getName() + '$' + name ).getAnnotation( Component.class );
    }

    private static Component replicate( final Component orig )
    {
        return new ComponentImpl( orig.role(), orig.hint(), orig.instantiationStrategy(), orig.description() );
    }

    public void testNullChecks()
    {
        checkNullNotAllowed( null, "", "", "" );
        checkNullNotAllowed( Object.class, null, "", "" );
        checkNullNotAllowed( Object.class, "", null, "" );
        checkNullNotAllowed( Object.class, "", "", null );
    }

    private static void checkNullNotAllowed( final Class<?> role, final String hint, final String instantationStrategy,
                                             final String description )
    {
        try
        {
            new ComponentImpl( role, hint, instantationStrategy, description );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
