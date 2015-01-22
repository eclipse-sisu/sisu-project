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
package org.eclipse.sisu.inject;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;

/**
 * Binding source locations can implement this interface to supply annotations to the {@link BeanLocator}.
 * 
 * @see Binder#withSource(Object)
 */
public interface AnnotatedSource
{
    /**
     * @param annotationType The annotation type
     * @return Annotation value; {@code null} if the annotation doesn't exist
     */
    <T extends Annotation> T getAnnotation( Class<T> annotationType );
}
