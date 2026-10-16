/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.context.Context;

public interface ContainerConfiguration {
    ContainerConfiguration setName(String name);

    ContainerConfiguration setContainerConfiguration(String configurationPath);

    String getContainerConfiguration();

    ContainerConfiguration setContainerConfigurationURL(URL configurationUrl);

    URL getContainerConfigurationURL();

    ContainerConfiguration setClassWorld(ClassWorld classWorld);

    ClassWorld getClassWorld();

    ContainerConfiguration setRealm(ClassRealm classRealm);

    ClassRealm getRealm();

    ContainerConfiguration setContext(Map<Object, Object> context);

    Map<Object, Object> getContext();

    ContainerConfiguration setComponentVisibility(String visibility);

    String getComponentVisibility();

    ContainerConfiguration setAutoWiring(boolean on);

    boolean getAutoWiring();

    ContainerConfiguration setClassPathScanning(String scanning);

    String getClassPathScanning();

    ContainerConfiguration setContextComponent(Context context);

    Context getContextComponent();

    ContainerConfiguration setJSR250Lifecycle(boolean on);

    boolean getJSR250Lifecycle();

    ContainerConfiguration setStrictClassPathScanning(boolean strictScanning);

    boolean getStrictClassPathScanning();

    ContainerConfiguration setJSR330ComponentVisibilityFollowsPlexusVisibility(boolean on);

    boolean getJSR330ComponentVisibilityFollowsPlexusVisibility();

    /**
     * Allows customizing visibility predicate supplier. If {@link #getJSR330ComponentVisibilityFollowsPlexusVisibility()}
     * is set to {@code true}, container will use the default predicate supplier. This method allows programmatic
     * altering of default supplier, for example by wrapping it, replacing it. The supplied function will receive
     * the default supplier, must return a non {@code null} instance. By default, this function is set to
     * {@link Function#identity()}.
     *
     * @param supplierTransformer the supplier transformer, may be {@code null} meaning "identity" (the default value).
     * @since 1.1.1
     */
    ContainerConfiguration setVisibilityPredicateSupplierTransformer(
            Function<Supplier<Predicate>, Supplier<Predicate>> supplierTransformer);

    /**
     * Returns the configured visibility predicate supplier, never {@code null}. The default transformer is
     * {@link Function#identity()}.
     *
     * @since 1.1.1
     */
    Function<Supplier<Predicate>, Supplier<Predicate>> getVisibilityPredicateSupplierTransformer();
}
