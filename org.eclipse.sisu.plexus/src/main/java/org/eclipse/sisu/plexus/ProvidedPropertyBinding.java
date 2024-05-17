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

import javax.inject.Provider;

import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.bean.PropertyBinding;

/**
 * Represents a {@link BeanProperty} bound to a {@link Provider}.
 */
final class ProvidedPropertyBinding<T>
    implements PropertyBinding
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanProperty<T> property;

    private final Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ProvidedPropertyBinding( final BeanProperty<T> property, final Provider<T> provider )
    {
        this.property = property;
        this.provider = provider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> void injectProperty( final B bean )
    {
        property.set( bean, provider.get() );
    }
}
