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

import java.io.File;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class FileTypeConverterTest
    extends TestCase
{
    public void testFileConversion()
    {
        final File file = Guice.createInjector( new FileTypeConverter(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "file" ) ).to( "work/temp" );
            }
        } ).getInstance( Key.get( File.class, Names.named( "file" ) ) );

        assertEquals( "work" + File.separator + "temp", file.getPath() );
    }
}
