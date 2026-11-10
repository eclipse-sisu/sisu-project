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
package org.eclipse.sisu.inject.classfile;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflection-based analysis shared by the {@link ProxyGenerator} implementations:
 * decides which facade methods a provider proxy must wrap for delegation.
 */
final class WrappableMethods {
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Map<String, Method> OBJECT_METHOD_MAP;

    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static {
        // pre-seed common methods that should be wrapped
        OBJECT_METHOD_MAP = new HashMap<>();
        for (Method m : Object.class.getMethods()) {
            if (isWrappable(m)) {
                OBJECT_METHOD_MAP.put(signatureKey(m), m);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private WrappableMethods() {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the methods that should be wrapped for delegation in the given class.
     */
    static Collection<Method> getWrappableMethods(Class<?> clazz) {
        Map<String, Method> methodMap = new HashMap<>(OBJECT_METHOD_MAP);
        for (Method m : clazz.getMethods()) {
            if (isWrappable(m)) {
                methodMap.put(signatureKey(m), m);
            }
        }
        return methodMap.values();
    }

    /**
     * Returns {@code true} if the given method is declared by {@link Object};
     * otherwise {@code false}.
     */
    static boolean isObjectMethod(Method method) {
        return OBJECT_METHOD_MAP.containsKey(signatureKey(method));
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns {@code true} if the given method should be wrapped; otherwise {@code false}.
     */
    private static boolean isWrappable(Method method) {
        return (method.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0;
    }

    /**
     * Returns a signature-based key that identifies the given method in the current class.
     */
    private static String signatureKey(Method method) {
        StringBuilder buf = new StringBuilder(method.getName());
        for (Class<?> t : method.getParameterTypes()) {
            buf.append(':').append(t);
        }
        return buf.toString();
    }
}
