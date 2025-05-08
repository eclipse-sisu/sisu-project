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

/**
 * Provides custom {@link PropertyBinding}s for bean properties such as fields or setter methods.
 */
public interface PropertyBinder
{
    /**
     * Returns the appropriate {@link PropertyBinding} for the given bean property.
     * 
     * @param property The bean property
     * @return Binding for the given property; {@code null} if no binding is applicable
     */
    <T> PropertyBinding bindProperty( BeanProperty<T> property );

    /**
     * Binders may return {@code LAST_BINDING} to indicate they are done binding a bean.
     */
    PropertyBinding LAST_BINDING = new PropertyBinding()
    {
        public <B> void injectProperty( final B bean )
        {
            throw new UnsupportedOperationException( "LAST_BINDING" );
        }
    };
}
