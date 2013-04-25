/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
/**
 * Bean scanning.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link ClassSpace}
 * <dd>Represents a source of classes and resources that can be scanned.
 * <dt>{@link SpaceModule}
 * <dd>Scans a {@link ClassSpace} for beans and adds any qualified bindings.
 * <dt>{@link ClassSpaceVisitor}
 * <dd>Something that can visit {@link ClassSpace}s.
 * <dt>{@link QualifiedTypeListener}
 * <dd>Listens out for types annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link QualifiedTypeVisitor}
 * <dd>{@link ClassSpaceVisitor} that reports types with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link SisuIndex}
 * <dd>Command-line indexing tool.
 * <dt>{@link SisuIndexAPT6}
 * <dd>Java 6 annotation processor.
 * </dl>
 */
package org.eclipse.sisu.space;

