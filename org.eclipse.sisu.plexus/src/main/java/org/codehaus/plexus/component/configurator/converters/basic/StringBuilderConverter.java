/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.codehaus.plexus.component.configurator.converters.basic;

public class StringBuilderConverter
    extends AbstractBasicConverter
{
    public boolean canConvert( final Class<?> type )
    {
        return StringBuilder.class.equals( type );
    }

    @Override
    public Object fromString( final String value )
    {
        return new StringBuilder( value );
    }
}
