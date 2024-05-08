/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.bean.alpha;

import org.eclipse.sisu.PostConstruct;
import org.eclipse.sisu.PreDestroy;

import javax.inject.Singleton;

@Singleton
public class SisuOverriddenPublic
    extends Private
{
    @Override
    @PostConstruct
    public void a()
    {
        results.append( "A" );
    }

    @Override
    @PreDestroy
    public void z()
    {
        results.append( "Z" );
    }
}
