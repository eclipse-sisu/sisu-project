/********************************************************************************
 * Copyright (c) 2023-present Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-1.0
 ********************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The PostConstruct annotation is used on a method that needs to be executed 
 * after dependency injection is done to perform any initialization. This 
 * method is invoked by Sisu before the class is put into service.
 * <p>
 * This annotation is Sisu specific annotation, that has same semantics as
 * {@link javax.annotation.PostConstruct} annotation has, and may be used
 * interchangeably.
 * <p>
 * To use annotation {@link org.eclipse.sisu.bean.LifecycleModule} needs to be
 * installed.
 *
 * @since TBD
 */
@Target( value = { ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface PostConstruct {
}
