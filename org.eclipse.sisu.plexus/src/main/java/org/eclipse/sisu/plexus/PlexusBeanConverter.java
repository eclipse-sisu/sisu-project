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

import com.google.inject.TypeLiteral;

/**
 * Service that converts values into various beans by following Plexus configuration rules.
 */
public interface PlexusBeanConverter
{
    /**
     * Converts the given constant value to a bean of the given type.
     * 
     * @param role The expected bean type
     * @param value The constant value
     * @return Bean of the given type, based on the given constant value
     */
    <T> T convert( TypeLiteral<T> role, String value );
}
