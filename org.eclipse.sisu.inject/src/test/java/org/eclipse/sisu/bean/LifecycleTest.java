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
package org.eclipse.sisu.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LifecycleTest {
    Injector injector;

    @BeforeEach
    void setUp() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new LifecycleModule());
            }
        });
    }

    @Test
    void testBaseLifecycle() {
        assertEquals(
                "a",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Public.class).results());
        assertEquals(
                "ab",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Protected.class)
                        .results());
        assertEquals(
                "abc",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Package.class).results());
        assertEquals(
                "abcd",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Private.class).results());

        injector.getInstance(BeanManager.class).unmanage();

        assertEquals(
                "az",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Public.class).results());
        assertEquals(
                "abyz",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Protected.class)
                        .results());
        assertEquals(
                "abcxyz",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Package.class).results());
        assertEquals(
                "abcdwxyz",
                injector.getInstance(org.eclipse.sisu.bean.alpha.Private.class).results());
    }

    @Test
    void testOverriddenLifecycle() {
        assertEquals(
                "bcdA",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPublic.class)
                        .results());
        assertEquals(
                "cdAB",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenProtected.class)
                        .results());
        assertEquals(
                "dABC",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPackage.class)
                        .results());
        assertEquals(
                "dABCD",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPrivate.class)
                        .results());

        injector.getInstance(BeanManager.class).unmanage();

        assertEquals(
                "bcdAZwxy",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPublic.class)
                        .results());
        assertEquals(
                "cdABYZwx",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenProtected.class)
                        .results());
        assertEquals(
                "dABCXYZw",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPackage.class)
                        .results());
        assertEquals(
                "dABCDWXYZw",
                injector.getInstance(org.eclipse.sisu.bean.alpha.OverriddenPrivate.class)
                        .results());
    }

    @Test
    void testHiddenLifecycle() {
        assertEquals(
                "bcd",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPublic.class)
                        .results());
        assertEquals(
                "cd",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenProtected.class)
                        .results());
        assertEquals(
                "d",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPackage.class)
                        .results());
        assertEquals(
                "d",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPrivate.class)
                        .results());

        injector.getInstance(BeanManager.class).unmanage();

        assertEquals(
                "bcdwxy",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPublic.class)
                        .results());
        assertEquals(
                "cdwx",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenProtected.class)
                        .results());
        assertEquals(
                "dw",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPackage.class)
                        .results());
        assertEquals(
                "dw",
                injector.getInstance(org.eclipse.sisu.bean.alpha.HiddenPrivate.class)
                        .results());
    }

    @Test
    void testOverriddenLifecycleInDifferentPackage() {
        assertEquals(
                "bcdA",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPublic.class)
                        .results());
        assertEquals(
                "cdAB",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenProtected.class)
                        .results());
        assertEquals(
                "cdABC",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPackage.class)
                        .results());
        assertEquals(
                "cdABCD",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPrivate.class)
                        .results());

        injector.getInstance(BeanManager.class).unmanage();

        assertEquals(
                "bcdAZwxy",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPublic.class)
                        .results());
        assertEquals(
                "cdABYZwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenProtected.class)
                        .results());
        assertEquals(
                "cdABCXYZwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPackage.class)
                        .results());
        assertEquals(
                "cdABCDWXYZwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.OverriddenPrivate.class)
                        .results());
    }

    @Test
    void testHiddenLifecycleInDifferentPackage() {
        assertEquals(
                "bcd",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPublic.class)
                        .results());
        assertEquals(
                "cd",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenProtected.class)
                        .results());
        assertEquals(
                "cd",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPackage.class)
                        .results());
        assertEquals(
                "cd",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPrivate.class)
                        .results());

        injector.getInstance(BeanManager.class).unmanage();

        assertEquals(
                "bcdwxy",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPublic.class)
                        .results());
        assertEquals(
                "cdwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenProtected.class)
                        .results());
        assertEquals(
                "cdwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPackage.class)
                        .results());
        assertEquals(
                "cdwx",
                injector.getInstance(org.eclipse.sisu.bean.beta.HiddenPrivate.class)
                        .results());
    }
}
