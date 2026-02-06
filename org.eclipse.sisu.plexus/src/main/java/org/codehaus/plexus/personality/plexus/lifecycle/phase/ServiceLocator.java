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
package org.codehaus.plexus.personality.plexus.lifecycle.phase;

import java.util.List;
import java.util.Map;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public interface ServiceLocator {
    Object lookup(String role) throws ComponentLookupException;

    Object lookup(String role, String hint) throws ComponentLookupException;

    Map<String, Object> lookupMap(String role) throws ComponentLookupException;

    List<Object> lookupList(String role) throws ComponentLookupException;

    void release(Object component) throws ComponentLifecycleException;

    void releaseAll(Map<String, ?> components) throws ComponentLifecycleException;

    void releaseAll(List<?> components) throws ComponentLifecycleException;

    boolean hasComponent(String role);

    boolean hasComponent(String role, String hint);
}
