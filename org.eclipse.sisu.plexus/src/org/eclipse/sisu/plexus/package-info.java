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
 * Implements Plexus semantics on top of Google-Guice.
 * <p><p>
 * Legacy applications can use the Plexus container API as usual:
 * <p>
 * <pre>
 * ContainerConfiguration config = new DefaultContainerConfiguration();
 * // ... configure ...
 * PlexusContainer container = null;
 * try {
 *   container = new DefaultPlexusContainer( config );
 *   // ... execute/wait ...
 * } finally {
 *   if ( container != null ) {
 *     container.dispose();
 *   }
 * }</pre>
 * 
 * Sisu applications that want to re-use Plexus components can use the {@link org.eclipse.sisu.plexus.PlexusSpaceModule} wrapper:
 * <p>
 * <pre>
 * binder.install( new PlexusSpaceModule( space ) );</pre>
 */
package org.eclipse.sisu.plexus;

