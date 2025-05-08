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
 * The PreDestroy annotation is used on methods as a callback notification to 
 * signal that the instance is in the process of being removed by the 
 * container. The method annotated with PreDestroy is typically used to 
 * release resources that it has been holding.
 * <p>
 * This annotation is Sisu specific annotation, that has same semantics as
 * {@link javax.annotation.PreDestroy} annotation has, and may be used
 * interchangeably.
 * <p>
 * To use annotation {@link org.eclipse.sisu.bean.LifecycleModule} needs to be
 * installed.
 *
 * @since 0.9.0.M2
 */

@Target( value = { ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface PreDestroy
{
}
