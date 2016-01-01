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
package org.eclipse.sisu.plexus;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * {@link Module}-like interface for contributing Plexus bindings with additional metadata.
 */
public interface PlexusBeanModule
{
    /**
     * Contributes bindings and returns any associated {@link PlexusBeanSource} metadata.
     * 
     * @see Module#configure(Binder)
     */
    PlexusBeanSource configure( Binder binder );
}
