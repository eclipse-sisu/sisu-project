/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
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
package org.codehaus.plexus.component.configurator.expression;

public final class ExpressionEvaluationException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public ExpressionEvaluationException( final String message )
    {
        super( message );
    }

    public ExpressionEvaluationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
