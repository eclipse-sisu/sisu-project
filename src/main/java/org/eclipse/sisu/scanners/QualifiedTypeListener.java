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
package org.eclipse.sisu.scanners;

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import com.google.inject.Binder;

/**
 * Listens for types annotated with {@link Qualifier} annotations.
 */
public interface QualifiedTypeListener
{
    /**
     * Invoked when the {@link QualifiedTypeVisitor} finds a qualified type.
     * 
     * @param qualifier The qualifier (unused)
     * @param qualifiedType The qualified type
     * @param source The source of this type
     * @see Binder#withSource(Object)
     */
    void hear( @Deprecated Annotation qualifier, Class<?> qualifiedType, Object source );
}
