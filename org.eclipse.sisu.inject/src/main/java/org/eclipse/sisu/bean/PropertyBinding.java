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
package org.eclipse.sisu.bean;

/**
 * Represents a bean property that has been bound by a {@link PropertyBinder}.
 */
public interface PropertyBinding
{
    /**
     * Injects the current bound value into the property of the given bean.
     * 
     * @param bean The bean to inject
     */
    <B> void injectProperty( B bean );
}
