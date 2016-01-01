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

/**
 * Constants representing supported Plexus instantiation strategies.
 */
public interface Strategies
{
    String LOAD_ON_START = "load-on-start";

    String PER_LOOKUP = "per-lookup";

    String SINGLETON = "singleton";
}
