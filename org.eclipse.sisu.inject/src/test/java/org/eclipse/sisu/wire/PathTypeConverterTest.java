/*******************************************************************************
 * Copyright (c) 2021-present Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.wire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathTypeConverterTest {
    @Test
    void testPathConversion() {
        final Path path = Guice.createInjector(new PathTypeConverter(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bindConstant().annotatedWith(Names.named("path")).to("work/temp");
                    }
                })
                .getInstance(Key.get(Path.class, Names.named("path")));

        assertEquals("work" + File.separator + "temp", path.toFile().getPath());
    }
}
