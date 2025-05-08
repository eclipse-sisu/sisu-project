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

import javax.inject.Qualifier;

/**
 * {@link Qualifier} of application parameters:<br>
 * <br>
 * 
 * <pre>
 * &#064;Inject
 * &#064;Parameters
 * String[] args;
 * 
 * &#064;Inject
 * &#064;Parameters
 * Map&lt;?, ?&gt; properties;
 * </pre>
 * 
 * <p>
 * This qualifier marks collections of values that act as overall application parameters, like the {@code String[]}
 * argument array passed into the main method or the {@code Map} of system properties. External parameters can be
 * supplied to Sisu by using the appropriate type along with the {@link Parameters} binding annotation.
 * 
 * <pre>
 * // add @Named for automatic installation
 * public class MyParametersModule
 *     extends AbstractModule
 * {
 *     &#064;Provides
 *     &#064;Parameters
 *     String[] customArgs()
 *     {
 *         return myArgs;
 *     }
 * 
 *     &#064;Provides
 *     &#064;Parameters
 *     Map&lt;?, ?&gt; customProperties()
 *     {
 *         return myProperties;
 *     }
 * 
 *     &#064;Override
 *     protected void configure()
 *     {
 *         // other setup
 *     }
 * }
 * </pre>
 * 
 * Tip: if you wrap {@link org.eclipse.sisu.wire.WireModule WireModule} around your set of application modules then it
 * will merge multiple &#064;{@link Parameters} bindings; for maps by providing an aggregate view over all bound maps,
 * for arrays by appending their elements into a single argument array.
 */
@Target( value = { ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
@Qualifier
public @interface Parameters
{
}
