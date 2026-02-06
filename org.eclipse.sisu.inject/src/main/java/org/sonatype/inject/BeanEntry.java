/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
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
package org.sonatype.inject;

import java.lang.annotation.Annotation;

/**
 * @deprecated Replaced by {@link org.eclipse.sisu.BeanEntry org.eclipse.sisu.BeanEntry}
 */
@Deprecated
public interface BeanEntry<Q extends Annotation, T> extends org.eclipse.sisu.BeanEntry<Q, T> {}
