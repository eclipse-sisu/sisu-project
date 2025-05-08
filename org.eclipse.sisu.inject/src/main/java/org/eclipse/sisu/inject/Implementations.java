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
package org.eclipse.sisu.inject;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.eclipse.sisu.Description;
import org.eclipse.sisu.Priority;

import com.google.inject.Binding;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * Utility methods for discovering the implementations behind Guice bindings.
 */
final class Implementations
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean hasGuiceServlet;
        try
        {
            hasGuiceServlet = BindingTargetVisitor.class.isInstance( ServletFinder.THIS );
        }
        catch ( final LinkageError e )
        {
            hasGuiceServlet = false;
        }
        HAS_GUICE_SERVLET = hasGuiceServlet;

        boolean hasJsr250Priority;
        try
        {
            hasJsr250Priority = javax.annotation.Priority.class.isAnnotation();
        }
        catch ( final LinkageError e )
        {
            hasJsr250Priority = false;
        }
        HAS_JSR250_PRIORITY = hasJsr250Priority;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_GUICE_SERVLET;

    private static final boolean HAS_JSR250_PRIORITY;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Implementations()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to find the implementation behind the given {@link Binding}.
     * 
     * @param binding The binding
     * @return Implementation class behind the binding; {@code null} if it couldn't be found
     */
    public static Class<?> find( final Binding<?> binding )
    {
        return binding.acceptTargetVisitor( ClassFinder.THIS );
    }

    /**
     * Attempts to find an annotation on the implementation behind the given {@link Binding}.
     * 
     * @param binding The binding
     * @param annotationType The annotation type
     * @return Annotation on the bound implementation; {@code null} if it couldn't be found
     */
    public static <T extends Annotation> T getAnnotation( final Binding<?> binding, final Class<T> annotationType )
    {
        final boolean isPriority = Priority.class.equals( annotationType );

        final Class<?> annotationSource =
            // when looking for @Priority also consider annotations on providers (and servlets/filters if available)
            binding.acceptTargetVisitor( isPriority ? ( HAS_GUICE_SERVLET ? ServletFinder.THIS : ProviderFinder.THIS )
                                                    : ClassFinder.THIS );

        T annotation = null;
        if ( null != annotationSource )
        {
            annotation = annotationSource.getAnnotation( annotationType );
            if ( null == annotation )
            {
                if ( HAS_JSR250_PRIORITY && isPriority )
                {
                    annotation = adaptJsr250( binding, annotationSource );
                }
                else if ( Description.class.equals( annotationType ) )
                {
                    annotation = adaptLegacy( binding, annotationSource );
                }
            }
        }
        return annotation;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private static <T extends Annotation> T adaptJsr250( final Binding<?> binding, final Class<?> clazz )
    {
        final javax.annotation.Priority jsr250 = clazz.getAnnotation( javax.annotation.Priority.class );
        return null != jsr250 ? (T) new PrioritySource( binding.getSource(), jsr250.value() ) : null;
    }

    @SuppressWarnings( { "unchecked", "deprecation" } )
    private static <T extends Annotation> T adaptLegacy( final Binding<?> binding, final Class<?> clazz )
    {
        final org.sonatype.inject.Description legacy = clazz.getAnnotation( org.sonatype.inject.Description.class );
        return null != legacy ? (T) new DescriptionSource( binding.getSource(), legacy.value() ) : null;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link BindingTargetVisitor} that attempts to find the implementations behind bindings.
     */
    static class ClassFinder
        extends DefaultBindingTargetVisitor<Object, Class<?>>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        static final BindingTargetVisitor<Object, Class<?>> THIS = new ClassFinder();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Class<?> visit( final UntargettedBinding<?> binding )
        {
            return binding.getKey().getTypeLiteral().getRawType();
        }

        @Override
        public Class<?> visit( final LinkedKeyBinding<?> binding )
        {
            // this assumes only one level of indirection: api-->impl
            return binding.getLinkedKey().getTypeLiteral().getRawType();
        }

        @Override
        public Class<?> visit( final ConstructorBinding<?> binding )
        {
            return binding.getConstructor().getDeclaringType().getRawType();
        }

        @Override
        public Class<?> visit( final InstanceBinding<?> binding )
        {
            return binding.getInstance().getClass();
        }

        @Override
        public Class<?> visit( final ProviderInstanceBinding<?> binding )
        {
            return peekBehind( Guice4.getProviderInstance( binding ) );
        }

        @Override
        public Class<?> visit( final ExposedBinding<?> binding )
        {
            return binding.getPrivateElements().getInjector().getBinding( binding.getKey() ).acceptTargetVisitor( this );
        }

        final Class<?> peekBehind( final Provider<?> provider )
        {
            if ( provider instanceof DeferredProvider<?> )
            {
                try
                {
                    // deferred providers let us peek at the underlying implementation type
                    return ( (DeferredProvider<?>) provider ).getImplementationClass().load();
                }
                catch ( final TypeNotPresentException e ) // NOPMD
                {
                    // fall-through
                }
            }
            return null;
        }
    }

    /**
     * {@link ClassFinder} that also returns {@link Provider} implementations.
     */
    static class ProviderFinder
        extends ClassFinder
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        @SuppressWarnings( "hiding" )
        static final BindingTargetVisitor<Object, Class<?>> THIS = new ProviderFinder();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Class<?> visit( final ProviderInstanceBinding<?> binding )
        {
            final Provider<?> provider = Guice4.getProviderInstance( binding );
            final Class<?> providedClass = peekBehind( provider );
            return null != providedClass ? providedClass : provider.getClass();
        }

        @Override
        public Class<?> visit( final ProviderKeyBinding<?> binding )
        {
            return binding.getProviderKey().getTypeLiteral().getRawType();
        }
    }

    /**
     * {@link ProviderFinder} that also returns servlet/filter implementations.
     */
    static final class ServletFinder
        extends ProviderFinder
        implements com.google.inject.servlet.ServletModuleTargetVisitor<Object, Class<?>>
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        @SuppressWarnings( "hiding" )
        static final BindingTargetVisitor<Object, Class<?>> THIS = new ServletFinder();

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Class<?> visit( final com.google.inject.servlet.InstanceFilterBinding binding )
        {
            return binding.getFilterInstance().getClass();
        }

        public Class<?> visit( final com.google.inject.servlet.InstanceServletBinding binding )
        {
            return binding.getServletInstance().getClass();
        }

        public Class<?> visit( final com.google.inject.servlet.LinkedFilterBinding binding )
        {
            // this assumes only one level of indirection: api-->impl
            return binding.getLinkedKey().getTypeLiteral().getRawType();
        }

        public Class<?> visit( final com.google.inject.servlet.LinkedServletBinding binding )
        {
            // this assumes only one level of indirection: api-->impl
            return binding.getLinkedKey().getTypeLiteral().getRawType();
        }
    }
}
