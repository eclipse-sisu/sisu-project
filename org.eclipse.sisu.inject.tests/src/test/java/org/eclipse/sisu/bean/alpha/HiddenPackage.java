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

import javax.inject.Singleton;

@Singleton
public class HiddenPackage
    extends HiddenProtected
{
    @Override
    void c()
    {
        results.append( "!" );
    }

    @Override
    void x()
    {
        results.append( "!" );
    }
}
