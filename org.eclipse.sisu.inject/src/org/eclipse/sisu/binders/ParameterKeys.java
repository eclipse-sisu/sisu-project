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
package org.eclipse.sisu.binders;

import java.util.Map;

import org.eclipse.sisu.Parameters;

import com.google.inject.Key;

@SuppressWarnings( "rawtypes" )
public interface ParameterKeys
{
    Key<Map> PROPERTIES = Key.get( Map.class, Parameters.class );

    Key<String[]> ARGUMENTS = Key.get( String[].class, Parameters.class );
}
