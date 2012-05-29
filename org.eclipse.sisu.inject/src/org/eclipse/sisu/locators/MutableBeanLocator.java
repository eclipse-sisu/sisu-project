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
package org.eclipse.sisu.locators;

import org.eclipse.sisu.locators.spi.BindingDistributor;
import org.eclipse.sisu.locators.spi.BindingPublisher;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

/**
 * Mutable {@link BeanLocator} that finds and tracks bindings across zero or more {@link BindingPublisher}s.
 */
@ImplementedBy( DefaultBeanLocator.class )
public interface MutableBeanLocator
    extends BeanLocator, BindingDistributor
{
    /**
     * Adds the given ranked {@link Injector} and distributes its {@link Binding}s. Marked as deprecated because most
     * clients should <b>not</b> call this method; any injector that contains a binding to the {@link BeanLocator} is
     * automatically added to that locator as part of the bootstrapping process.
     * 
     * @param injector The new injector
     * @param rank The assigned rank
     */
    @Deprecated
    void add( Injector injector, int rank );

    /**
     * Removes the given {@link Injector} and its {@link Binding}s.
     * 
     * @param injector The old injector
     */
    void remove( Injector injector );
}
