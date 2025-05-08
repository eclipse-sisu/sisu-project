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
package org.eclipse.sisu.bean;

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
