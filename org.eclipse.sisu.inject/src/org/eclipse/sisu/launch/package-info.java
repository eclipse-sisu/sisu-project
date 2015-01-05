/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * Utilities to test, launch, and extend Sisu applications. 
 * <p><p>
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
 * <p><p>
 * <pre>
 * java -classpath myapp.jar:javax.inject.jar:aopalliance.jar:guice-3.0.jar:org.eclipse.sisu.inject.jar org.eclipse.sisu.launch.Main</pre> 
 * 
 * An OSGi {@link org.eclipse.sisu.launch.SisuExtender extender} that assembles Sisu applications from OSGi bundles containing JSR330 components.
 * <p><p>
 * And a helper class that discovers Sisu {@link org.eclipse.sisu.launch.SisuExtensions extensions} registered under {@code META-INF/services}.
 */
package org.eclipse.sisu.launch;

