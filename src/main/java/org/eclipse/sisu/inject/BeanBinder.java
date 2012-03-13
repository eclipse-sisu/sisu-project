/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Provides custom {@link PropertyBinder}s for beans that contain one or more properties.
 */
public interface BeanBinder
{
    /**
     * Returns the appropriate {@link PropertyBinder} for the given bean type.
     * 
     * @param type The bean type
     * @param encounter The Guice type encounter
     * @return Property binder for the given type; {@code null} if no binder is applicable
     */
    <B> PropertyBinder bindBean( TypeLiteral<B> type, TypeEncounter<B> encounter );
}
