/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

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
