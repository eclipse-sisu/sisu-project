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

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;

@Category( org.eclipse.sisu.BaseTests.class )
public class TokensTest
    extends TestCase
{
    public void testTokenSplittingByComma()
    {
        assertNoTokens( "" );
        assertNoTokens( " " );
        assertNoTokens( "," );
        assertNoTokens( "  " );
        assertNoTokens( " ," );
        assertNoTokens( ", " );
        assertNoTokens( ",," );
        assertNoTokens( "   " );
        assertNoTokens( "  ," );
        assertNoTokens( " , " );
        assertNoTokens( " ,," );
        assertNoTokens( ",  " );
        assertNoTokens( ", ," );
        assertNoTokens( ",, " );
        assertNoTokens( ",,," );

        Iterator<String> itr;

        itr = Tokens.splitByComma( "foo" ).iterator();
        assertEquals( "foo", itr.next() );
        assertFalse( itr.hasNext() );

        itr = Tokens.splitByComma( " foo " ).iterator();
        assertEquals( "foo", itr.next() );
        assertFalse( itr.hasNext() );

        itr = Tokens.splitByComma( ",foo," ).iterator();
        assertEquals( "foo", itr.next() );
        assertFalse( itr.hasNext() );

        itr = Tokens.splitByComma( " ,  ,,   ,,,  foo ,  ,,   ,,," ).iterator();
        assertEquals( "foo", itr.next() );
        assertFalse( itr.hasNext() );

        itr = Tokens.splitByComma( " ,  foo ,,   ,,,  bar,  ,,baz   ,,,foo," ).iterator();
        assertEquals( "foo", itr.next() );
        assertEquals( "bar", itr.next() );
        assertEquals( "baz", itr.next() );
        assertEquals( "foo", itr.next() );
        assertFalse( itr.hasNext() );
    }

    private void assertNoTokens( final String text )
    {
        assertFalse( Tokens.splitByComma( text ).iterator().hasNext() );
    }
}
