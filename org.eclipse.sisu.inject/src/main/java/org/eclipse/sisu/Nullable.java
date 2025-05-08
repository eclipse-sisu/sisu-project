/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
package org.eclipse.sisu;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks dependencies that can be {@code null}:<br>
 * <br>
 * 
 * <pre>
 * &#064;Inject
 * &#064;Nullable
 * Component optional;
 * </pre>
 */
@Target( value = { ElementType.FIELD, ElementType.PARAMETER } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Nullable
{
}
