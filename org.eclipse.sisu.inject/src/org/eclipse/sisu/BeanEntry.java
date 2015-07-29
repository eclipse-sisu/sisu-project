/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.inject.Provider;
import javax.inject.Qualifier;

/**
 * Describes {@link Q}ualified bean implementations of {@link T}:<br>
 * <br>
 * 
 * <pre>
 * &#064;Inject
 * Iterable&lt;BeanEntry&lt;Named, Command&gt;&gt; commands;
 * </pre>
 * 
 * Use this when you want to know more about available beans; especially if you want to avoid creating instances.
 * 
 * @see org.eclipse.sisu.inject.BeanLocator
 */
public interface BeanEntry<Q extends Annotation, T>
    extends Entry<Q, T>
{
    /**
     * Returns the @{@link Qualifier} annotation associated with this particular bean.
     * 
     * @return Qualifier annotation
     */
    Q getKey();

    /**
     * Returns the associated instance of the bean; returns same instance for each call.
     * 
     * @return Bean instance (lazily-created)
     */
    T getValue();

    /**
     * Returns the underlying {@link Provider}; may support creation of multiple instances.
     * 
     * @return Bean provider
     */
    Provider<T> getProvider();

    /**
     * Returns a human-readable description of the bean; see @{@link Description}.
     * 
     * @return Human-readable description
     * @see Description
     */
    String getDescription();

    /**
     * Attempts to find the implementation type without creating the bean instance.
     * 
     * @return Implementation type; {@code null} if the type cannot be determined
     */
    Class<T> getImplementationClass();

    /**
     * Returns an arbitrary object that indicates where this bean was configured.
     * 
     * @return Source location
     */
    Object getSource();

    /**
     * Returns the bean's rank; higher ranked beans override lower ranked beans.
     * 
     * @return Assigned rank
     * @see Priority
     */
    int getRank();
}
