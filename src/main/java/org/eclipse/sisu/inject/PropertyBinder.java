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

import org.eclipse.sisu.reflect.BeanProperty;

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
