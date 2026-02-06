/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
 * Service that manages the lifecycle of beans.
 */
public interface BeanManager {
    /**
     * Decides whether instances of the given bean type should be reported to this manager.
     *
     * @param clazz The bean type
     * @return {@code true} if instances of the bean should be reported; otherwise {@code false}
     */
    boolean manage(Class<?> clazz);

    /**
     * Asks this manager to manage the given bean property.
     *
     * @param property The bean property
     * @return Non-null binding if the bean property was managed; otherwise {@code null}
     */
    PropertyBinding manage(BeanProperty<?> property);

    /**
     * Asks this manager to manage the given bean instance.
     *
     * @param bean The bean instance
     * @return {@code true} if the bean instance was managed; otherwise {@code false}
     */
    boolean manage(Object bean);

    /**
     * Asks this manager to unmanage the given bean instance.
     *
     * @param bean The bean instance
     * @return {@code true} if the bean instance was unmanaged; otherwise {@code false}
     */
    boolean unmanage(Object bean);

    /**
     * Asks this manager to unmanage all the bean instances it knows about.
     *
     * @return {@code true} if any bean instances were unmanaged; otherwise {@code false}
     */
    boolean unmanage();
}
