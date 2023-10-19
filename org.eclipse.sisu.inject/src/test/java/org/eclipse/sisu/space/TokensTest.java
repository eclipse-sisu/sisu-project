/*******************************************************************************
 * Copyright (c) 2021-present Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.space;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BaseTests
class TokensTest
{
    @Test
    void testTokenSplittingByComma()
    {
        assertNoTokens( Tokens.splitByComma( "" ) );
        assertNoTokens( Tokens.splitByComma( " " ) );
        assertNoTokens( Tokens.splitByComma( "," ) );
        assertNoTokens( Tokens.splitByComma( "  " ) );
        assertNoTokens( Tokens.splitByComma( " ," ) );
        assertNoTokens( Tokens.splitByComma( ", " ) );
        assertNoTokens( Tokens.splitByComma( ",," ) );
        assertNoTokens( Tokens.splitByComma( "   " ) );
        assertNoTokens( Tokens.splitByComma( "  ," ) );
        assertNoTokens( Tokens.splitByComma( " , " ) );
        assertNoTokens( Tokens.splitByComma( " ,," ) );
        assertNoTokens( Tokens.splitByComma( ",  " ) );
        assertNoTokens( Tokens.splitByComma( ", ," ) );
        assertNoTokens( Tokens.splitByComma( ",, " ) );
        assertNoTokens( Tokens.splitByComma( ",,," ) );

        assertTokens( Tokens.splitByComma( "foo" ), "foo" );
        assertTokens( Tokens.splitByComma( " foo " ), "foo" );
        assertTokens( Tokens.splitByComma( ",foo," ), "foo" );
        assertTokens( Tokens.splitByComma( " ,  ,,   ,,,  foo ,  ,,   ,,," ), "foo" );
        assertTokens( Tokens.splitByComma( " ,  foo ,,   ,,,  bar,  ,,baz   ,,,foo," ), "foo", "bar", "baz", "foo" );
    }

    @Test
    void testTokenSplittingByStar()
    {
        assertNoTokens( Tokens.splitByStar( "" ) );
        assertTokens( Tokens.splitByStar( " " ), " " );
        assertNoTokens( Tokens.splitByStar( "*" ) );
        assertTokens( Tokens.splitByStar( "  " ), "  " );
        assertTokens( Tokens.splitByStar( " *" ), " " );
        assertTokens( Tokens.splitByStar( "* " ), " " );
        assertNoTokens( Tokens.splitByStar( "**" ) );
        assertTokens( Tokens.splitByStar( "   " ), "   " );
        assertTokens( Tokens.splitByStar( "  *" ), "  " );
        assertTokens( Tokens.splitByStar( " * " ), " ", " " );
        assertTokens( Tokens.splitByStar( " **" ), " " );
        assertTokens( Tokens.splitByStar( "*  " ), "  " );
        assertTokens( Tokens.splitByStar( "* *" ), " " );
        assertTokens( Tokens.splitByStar( "** " ), " " );
        assertNoTokens( Tokens.splitByStar( "***" ) );

        assertTokens( Tokens.splitByStar( "foo" ), "foo" );
        assertTokens( Tokens.splitByStar( " foo " ), " foo " );
        assertTokens( Tokens.splitByStar( "*foo*" ), "foo" );
        assertTokens( Tokens.splitByStar( " *  **   ***  foo *  **   ***" ), " ", "  ", "   ", "  foo ", "  ", "   " );
        assertTokens( Tokens.splitByStar( " *  foo **   ***  bar*  **baz   ***foo*" ), " ", "  foo ", "   ", "  bar",
                      "  ", "baz   ", "foo" );
    }

    private void assertNoTokens( final Iterable<String> tokens )
    {
        Iterator<String> itr = tokens.iterator();
        assertFalse( itr.hasNext() );
        assertThrows( NoSuchElementException.class, () -> itr.next() );
    }

    private void assertTokens( final Iterable<String> tokens, final String... expected )
    {
        Iterator<String> itr = tokens.iterator();
        assertTrue( itr.hasNext() );
        for ( int i = 0; i < expected.length; i++ )
        {
            assertEquals( expected[i], itr.next() );
        }
        assertFalse( itr.hasNext() );
        assertThrows( NoSuchElementException.class, () -> itr.next() );
    }
}
