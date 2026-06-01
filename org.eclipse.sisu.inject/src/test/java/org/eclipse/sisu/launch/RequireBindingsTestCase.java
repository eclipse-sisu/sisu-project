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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.inject.Binder;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 version.
 */
public final class RequireBindingsTestCase extends InjectedTest {
    @Override
    public void configure(final Binder binder) {
        binder.requireExplicitBindings();
    }

    @Inject
    Foo bean;

    @Test
    public void testRequireExplicitBindings() {
        assertNotNull(bean);
    }
}
