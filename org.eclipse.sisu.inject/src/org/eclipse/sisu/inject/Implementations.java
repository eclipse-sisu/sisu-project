/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.inject;

import com.google.inject.Binding;
import com.google.inject.Provider;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * Utility methods for discovering the implementations behind Guice bindings.
 */
public final class Implementations
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean hasExtensions;
        try
        {
            hasExtensions = BindingTargetVisitor.class.isInstance( ServletFinder.THIS );
        }
        catch ( final LinkageError e )
        {
            hasExtensions = false;
        }
        HAS_EXTENSIONS = hasExtensions;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean HAS_EXTENSIONS;

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
     * Attempts to find the implementation behind the given {@link Binding}. Ignores any extension-specific bindings
     * such as servlet/filter definitions, where the actual implementation is hidden inside the definition instance.
     * 
     * @param binding The Guice binding
     * @return Implementation class behind the binding; {@code null} if it couldn't be found
     */
    public static Class<?> find( final Binding<?> binding )
    {
        return binding.acceptTargetVisitor( ClassFinder.THIS );
    }

    /**
     * Attempts to find the implementation behind the given {@link Binding}; can peek inside servlet/filter definitions.
     * 
     * @param binding The Guice binding
     * @return Implementation class behind the binding; {@code null} if it couldn't be found
     */
    public static Class<?> extendedFind( final Binding<?> binding )
    {
        return binding.acceptTargetVisitor( HAS_EXTENSIONS ? ServletFinder.THIS : ClassFinder.THIS );
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
            final Provider<?> provider = binding.getProviderInstance();
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

        @Override
        public Class<?> visit( final ExposedBinding<?> binding )
        {
            return binding.getPrivateElements().getInjector().getBinding( binding.getKey() ).acceptTargetVisitor( this );
        }
    }

    /**
     * {@link ClassFinder} that can also peek behind serlvet/filter bindings.
     */
    static final class ServletFinder
        extends ClassFinder
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
