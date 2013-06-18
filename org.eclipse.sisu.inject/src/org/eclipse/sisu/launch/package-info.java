/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * Bean containers.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link Main}
 * <dd>Classic main entry point that creates a static {@link com.google.inject.Injector} for the current class-path.
 * <dt>{@link InjectedTestCase}
 * <dd>JUnit {@link junit.framework.TestCase} that automatically binds and injects itself.
 * <dt>{@link InjectedTest}
 * <dd>JUnit4/TestNG annotated class that automatically binds and injects itself.
 * </dl>
 */
package org.eclipse.sisu.launch;

