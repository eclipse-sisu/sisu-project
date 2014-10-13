/*******************************************************************************
 * Copyright (c) 2008, 2014 Stuart McCulloch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch - initial API and implementation
 *******************************************************************************/

package org.eclipse.sisu.peaberry.util.decorators;

import org.eclipse.sisu.peaberry.Import;
import org.eclipse.sisu.peaberry.builders.ImportDecorator;

/**
 * An {@link ImportDecorator} that applies decorators in a chain, right-left.
 * 
 * @author mcculls@gmail.com (Stuart McCulloch)
 */
public final class DecoratorChain<S>
    implements ImportDecorator<S> {

  private final ImportDecorator<S>[] decorators;

  public DecoratorChain(final ImportDecorator<S>... decorators) {
    this.decorators = decorators.clone();
  }

  public <T extends S> Import<T> decorate(final Import<T> service) {
    Import<T> decoratedService = service;
    for (int i = decorators.length - 1; 0 <= i; i--) {
      decoratedService = decorators[i].decorate(decoratedService);
    }
    return decoratedService;
  }
}
