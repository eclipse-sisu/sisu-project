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
package org.codehaus.plexus.context;

import java.util.Map;

public interface Context
{
    boolean contains( Object key );

    void put( Object key, Object value );

    Object get( Object key )
        throws ContextException;

    Map<Object, Object> getContextData();
}
