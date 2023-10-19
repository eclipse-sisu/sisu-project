/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.launch;

import javax.inject.Inject;

import com.google.inject.Binder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Custom2Test
    extends InjectedTest
{
    @Override
    public void configure( final Binder binder )
    {
        // override automatic binding
        binder.bind( Foo.class ).to( TaggedFoo.class );
    }

    @Inject
    Foo bean;

    @Test
    // @org.junit.jupiter.api.Test
    // @org.testng.annotations.Test
    public void testPerTestCaseCustomization()
    {
        assertTrue( bean instanceof TaggedFoo );
    }
}
