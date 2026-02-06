/*******************************************************************************
 * Copyright (c) 2014 Takari, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takari, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.io.File;
import junit.framework.TestCase;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class RealmDisposalTest extends TestCase {
    private ClassLoader origCL;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        origCL = Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(origCL);
        super.tearDown();
    }

    public void test441254_recreateChildRealm() throws Exception {
        final String realmId = "child-realm";

        final DefaultPlexusContainer plexus = new DefaultPlexusContainer();

        ClassRealm realm = plexus.createChildRealm(realmId);
        realm.addURL(new File("target/test-classes/component-jar/component-jar-0.1.jar")
                .getCanonicalFile()
                .toURI()
                .toURL());
        Thread.currentThread().setContextClassLoader(realm);
        plexus.discoverComponents(realm);
        assertNotNull(plexus.lookup("org.eclipse.sisu.plexus.tests.SomeComponent"));

        realm.getWorld().disposeRealm(realm.getId());

        realm = plexus.createChildRealm(realmId);
        realm.addURL(new File("target/test-classes/component-jar/component-jar-0.1.jar")
                .getCanonicalFile()
                .toURI()
                .toURL());
        Thread.currentThread().setContextClassLoader(realm);
        plexus.discoverComponents(realm);
        assertNotNull(plexus.lookup("org.eclipse.sisu.plexus.tests.SomeComponent"));
    }

    // the point of this disabled test is to manually assert all references to the disposed realms are cleared
    // the test runs create/dispose the same realm 100k times and prints 1k iterations how long it took
    // each 1k are expected to take about the same time to execute (as opposed to always increasing time)
    // it is recommended to run the test with -Xmx128m to make sure there are no memory leaks
    public void _test441254_torture() throws Exception {
        final String realmId = "child-realm";

        final DefaultPlexusContainer plexus = new DefaultPlexusContainer();

        ClassRealm realm = plexus.createChildRealm(realmId);
        realm.addURL(new File("target/test-classes/component-jar/component-jar-0.1.jar")
                .getCanonicalFile()
                .toURI()
                .toURL());
        Thread.currentThread().setContextClassLoader(realm);
        plexus.discoverComponents(realm);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            realm.getWorld().disposeRealm(realm.getId());

            realm = plexus.createChildRealm(realmId);
            realm.addURL(new File("target/test-classes/component-jar/component-jar-0.1.jar")
                    .getCanonicalFile()
                    .toURI()
                    .toURL());
            Thread.currentThread().setContextClassLoader(realm);
            plexus.discoverComponents(realm);

            if (i % 1000 == 0) {
                final long end = System.currentTimeMillis();
                System.out.printf("%6d %d\n", i, end - start);
                start = end;
            }
        }

        realm.getWorld().disposeRealm(realm.getId());

        System.in.read();
    }
}
