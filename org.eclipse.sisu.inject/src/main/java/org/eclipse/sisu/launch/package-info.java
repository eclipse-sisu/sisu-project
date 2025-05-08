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
/**
 * Utilities to test, launch, and extend Sisu applications. 
 * <p>
 * For example test classes that scan, bind, and auto-wire the test classpath:
 * <pre>
 * &#064;Test
 * public class MyJUnit4orTestNGTest extends {@link org.eclipse.sisu.launch.InjectedTest} {
 *   // ...tests...
 * }
 * 
 * public class MyJUnit3TestCase extends {@link org.eclipse.sisu.launch.InjectedTestCase} {
 *   // ...tests...
 * }</pre>
 * 
 * A {@link org.eclipse.sisu.launch.Main} class that launches Sisu applications from the command-line:
 * 
 * <pre>
 * java -classpath myapp.jar:javax.inject.jar:aopalliance.jar:guice-3.0.jar:org.eclipse.sisu.inject.jar org.eclipse.sisu.launch.Main</pre> 
 * 
 * An OSGi {@link org.eclipse.sisu.launch.SisuExtender extender} that assembles Sisu applications from OSGi bundles containing JSR330 components.
 * <p>
 * And a helper class that discovers Sisu {@link org.eclipse.sisu.launch.SisuExtensions extensions} registered under {@code META-INF/services}.
 */
package org.eclipse.sisu.launch;
