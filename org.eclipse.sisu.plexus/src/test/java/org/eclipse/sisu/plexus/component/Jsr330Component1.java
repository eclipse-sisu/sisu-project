/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konrad Windszus - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus.component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class Jsr330Component1 {

    protected final Jsr330Component2 component2;

    @Inject
    public Jsr330Component1(Jsr330Component2 component2) {
        this.component2 = component2;
    }
}
