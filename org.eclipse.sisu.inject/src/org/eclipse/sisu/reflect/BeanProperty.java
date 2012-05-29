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
package org.eclipse.sisu.reflect;

import java.lang.annotation.Annotation;

import com.google.inject.TypeLiteral;

/**
 * Represents a bean property such as a field or setter method.
 * 
 * <pre>
 * // like...
 * &#064;SomeAnnotation
 * SomeType someProperty;
 * 
 * // ...or...
 * &#064;SomeAnnotation
 * void setSomeProperty( SomeType _someProperty )
 * {
 *     // ...etc...
 * }
 * </pre>
 */
public interface BeanProperty<T>
{
    /**
     * Returns the property annotation with the specified type.
     * 
     * @param annotationType The annotation type
     * @return Property annotation if it exists; otherwise {@code null}
     */
    <A extends Annotation> A getAnnotation( Class<A> annotationType );

    /**
     * Returns the reified generic type of the property; for example {@code TypeLiteral<List<String>>}.
     * 
     * @return Reified generic type
     */
    TypeLiteral<T> getType();

    /**
     * Returns the normalized property name excluding the namespace; for example {@code "address"}.
     * 
     * @return Normalized property name
     */
    String getName();

    /**
     * Sets the property in the given bean to the given value.
     * 
     * @param bean The bean to update
     * @param value The value to set
     */
    <B> void set( B bean, T value );
}
