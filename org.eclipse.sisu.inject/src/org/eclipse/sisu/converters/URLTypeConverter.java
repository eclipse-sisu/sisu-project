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
package org.eclipse.sisu.converters;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

/**
 * {@link TypeConverter} {@link Module} that converts constants to {@link URL}s.
 */
public final class URLTypeConverter
    extends AbstractTypeConverter<URL>
{
    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        try
        {
            return new URL( value );
        }
        catch ( final MalformedURLException e )
        {
            throw new ProvisionException( e.toString() );
        }
    }
}
