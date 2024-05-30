/*
 * Copyright (c) 2010-2024 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject;

import com.google.inject.Binding;

/**
 * Publisher of {@link Binding}s to interested {@link BindingSubscriber}s.
 */
public interface BindingPublisher
{
    /**
     * Subscribes the given {@link BindingSubscriber} to receive {@link Binding}s.
     * 
     * @param subscriber The subscriber
     */
    <T> void subscribe( BindingSubscriber<T> subscriber );

    /**
     * Stops the given {@link BindingSubscriber} from receiving {@link Binding}s.
     * 
     * @param subscriber The subscriber
     */
    <T> void unsubscribe( BindingSubscriber<T> subscriber );

    /**
     * Estimates the maximum rank this publisher may assign to a {@link Binding}.
     * 
     * @return Maximum binding rank
     */
    int maxBindingRank();

    /**
     * Attempts to adapt this publisher to the given type.
     * 
     * @param type The target type
     * @return Adapted instance; {@code null} if it couldn't be adapted
     * @since 0.9.0.M1
     */
    <T> T adapt( Class<T> type );
}
