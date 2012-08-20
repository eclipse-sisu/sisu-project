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
package org.eclipse.sisu.plexus.binders;

import org.eclipse.sisu.inject.PropertyBinding;
import org.eclipse.sisu.reflect.BeanProperty;

/**
 * Service that manages the lifecycle of Plexus beans.
 */
public interface PlexusBeanManager
{
    /**
     * Decides whether instances of the given Plexus bean type should be reported to this manager.
     * 
     * @param clazz The Plexus bean type
     * @return {@code true} if instances of the bean should be reported; otherwise {@code false}
     */
    boolean manage( Class<?> clazz );

    /**
     * Asks this manager to manage the given bean property.
     * 
     * @param property The bean property
     * @return Non-null binding if the bean property was managed; otherwise {@code null}
     */
    PropertyBinding manage( BeanProperty<?> property );

    /**
     * Asks this manager to manage the given Plexus bean instance.
     * 
     * @param bean The Plexus bean instance
     * @return {@code true} if the bean instance was managed; otherwise {@code false}
     */
    boolean manage( Object bean );

    /**
     * Asks this manager to unmanage the given Plexus bean instance.
     * 
     * @param bean The Plexus bean instance
     * @return {@code true} if the bean instance was unmanaged; otherwise {@code false}
     */
    boolean unmanage( Object bean );

    /**
     * Asks this manager to unmanage all the Plexus bean instances it knows about.
     * 
     * @return {@code true} if any bean instances were unmanaged; otherwise {@code false}
     */
    boolean unmanage();
}
