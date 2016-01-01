/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component;

import java.util.Map;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.repository.ComponentRequirement;

public interface MapOrientedComponent
{
    void addComponentRequirement( ComponentRequirement requirement, Object value )
        throws ComponentConfigurationException;

    void setComponentConfiguration( Map<?, ?> configuration )
        throws ComponentConfigurationException;
}
