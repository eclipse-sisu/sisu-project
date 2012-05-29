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

import javax.inject.Inject;

import com.google.inject.Binder;

public final class Custom1TestCase
    extends InjectedTestCase
{
    @Override
    public void configure( final Binder binder )
    {
        // override automatic binding
        binder.bind( Foo.class ).to( NamedFoo.class );
    }

    @Inject
    Foo bean;

    public void testPerTestCaseCustomization()
    {
        assertTrue( bean instanceof NamedFoo );
    }
}
