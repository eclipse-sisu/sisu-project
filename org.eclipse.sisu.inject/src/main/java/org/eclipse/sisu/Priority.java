/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
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
 * Defines the priority ordering of a bean, higher values have higher priority:<br>
 * <br>
 * 
 * <pre>
 * &#064;Named
 * &#064;Priority( 999 )
 * public class ImportantComponent
 * {
 *     //
 * }
 * </pre>
 * 
 * Implicit priorities start at zero for default beans, negative for non-default.
 * 
 * @see org.eclipse.sisu.inject.RankingFunction
 * @see BeanEntry#getRank()
 */
@Target( value = { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Priority
{
    int value();
}
