/*******************************************************************************
 * Copyright (c) 2008, 2014 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/

package org.eclipse.sisu.peaberry.util;

import static com.google.inject.util.Types.newParameterizedType;

import org.eclipse.sisu.peaberry.Export;

import com.google.inject.TypeLiteral;

/**
 * Methods for creating {@link TypeLiteral}s that can be used with peaberry.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class TypeLiterals {

  // instances not allowed
  private TypeLiterals() {}

  /**
   * Create {@link TypeLiteral} matching an iterable sequence of the given type.
   * 
   * @param clazz service interface
   * @return literal type matching {@code Iterable<? extends T>}
   */
  @SuppressWarnings("unchecked")
  public static <T> TypeLiteral<Iterable<? extends T>> iterable(final Class<T> clazz) {
    return (TypeLiteral) TypeLiteral.get(newParameterizedType(Iterable.class, clazz));
  }

  /**
   * Create {@link TypeLiteral} matching an exported handle of the given type.
   * 
   * @param clazz service interface
   * @return literal type matching {@code Export<? extends T>}
   */
  @SuppressWarnings("unchecked")
  public static <T> TypeLiteral<Export<? extends T>> export(final Class<T> clazz) {
    return (TypeLiteral) TypeLiteral.get(newParameterizedType(Export.class, clazz));
  }
}
