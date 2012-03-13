/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.binders;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.enterprise.inject.Typed;
import javax.inject.Provider;

import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.locators.BeanLocator;
import org.eclipse.sisu.locators.WildcardKey;
import org.eclipse.sisu.reflect.TypeParameters;
import org.eclipse.sisu.scanners.QualifiedTypeListener;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * {@link QualifiedTypeListener} that installs {@link Module}s, registers {@link Mediator}s, and binds types.
 */
public final class QualifiedTypeBinder
    implements QualifiedTypeListener
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<?> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder rootBinder;

    private BeanListener beanListener;

    private Object currentSource;

    private Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedTypeBinder( final Binder binder )
    {
        rootBinder = binder;
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void hear( final Annotation qualifier, final Class qualifiedType, final Object source )
    {
        if ( currentSource != source )
        {
            if ( null != source )
            {
                binder = rootBinder.withSource( source );
                currentSource = source;
            }
            else
            {
                binder = rootBinder;
                currentSource = null;
            }
        }

        if ( !TypeParameters.isConcrete( qualifiedType ) )
        {
            return;
        }
        else if ( Module.class.isAssignableFrom( qualifiedType ) )
        {
            installModule( qualifiedType );
        }
        else if ( Mediator.class.isAssignableFrom( qualifiedType ) )
        {
            registerMediator( qualifiedType );
        }
        else if ( Provider.class.isAssignableFrom( qualifiedType ) )
        {
            bindProviderType( qualifiedType );
        }
        else
        {
            bindQualifiedType( qualifiedType );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Installs an instance of the given {@link Module}.
     * 
     * @param moduleType The module type
     */
    private void installModule( final Class<Module> moduleType )
    {
        final Module module = newInstance( moduleType );
        if ( null != module )
        {
            binder.install( module );
        }
    }

    /**
     * Registers an instance of the given {@link Mediator} using its generic type parameters as configuration.
     * 
     * @param mediatorType The mediator type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void registerMediator( final Class<Mediator> mediatorType )
    {
        final TypeLiteral<?>[] params = getSuperTypeParameters( mediatorType, Mediator.class );
        if ( params.length != 3 )
        {
            binder.addError( mediatorType + " has wrong number of type arguments" );
        }
        else
        {
            final Mediator mediator = newInstance( mediatorType );
            if ( null != mediator )
            {
                mediate( Key.get( params[1], (Class) params[0].getRawType() ), mediator, params[2].getRawType() );
            }
        }
    }

    /**
     * Uses the given mediator to mediate updates between the {@link BeanLocator} and associated watchers.
     * 
     * @param key The watched key
     * @param mediator The bean mediator
     * @param watcherType The watcher type
     */
    @SuppressWarnings( "rawtypes" )
    private void mediate( final Key key, final Mediator mediator, final Class watcherType )
    {
        if ( null == beanListener )
        {
            beanListener = new BeanListener();
            binder.bindListener( Matchers.any(), beanListener );
            binder.requestInjection( beanListener );
        }
        beanListener.mediate( key, mediator, watcherType );
    }

    /**
     * Binds the given provider type using a binding key determined by common-use heuristics.
     * 
     * @param providerType The provider type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void bindProviderType( final Class<?> providerType )
    {
        final TypeLiteral[] params = getSuperTypeParameters( providerType, javax.inject.Provider.class );
        if ( params.length != 1 )
        {
            binder.addError( providerType + " has wrong number of type arguments" );
        }
        else
        {
            final Key key = getBindingKey( params[0], getBindingName( providerType ) );
            final ScopedBindingBuilder sbb = binder.bind( key ).toProvider( providerType );
            if ( providerType.isAnnotationPresent( EagerSingleton.class ) )
            {
                sbb.asEagerSingleton();
            }
            else if ( providerType.isAnnotationPresent( javax.inject.Singleton.class )
                || providerType.isAnnotationPresent( com.google.inject.Singleton.class ) )
            {
                sbb.in( Scopes.SINGLETON );
            }

            final Class<?>[] types = getBindingTypes( providerType );
            if ( null != types )
            {
                for ( final Class bindingType : types )
                {
                    binder.bind( key.ofType( bindingType ) ).to( key );
                }
            }
        }
    }

    /**
     * Binds the given qualified type using a binding key determined by common-use heuristics.
     * 
     * @param qualifiedType The qualified type
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void bindQualifiedType( final Class<?> qualifiedType )
    {
        final ScopedBindingBuilder sbb = binder.bind( qualifiedType );
        if ( qualifiedType.isAnnotationPresent( EagerSingleton.class ) )
        {
            sbb.asEagerSingleton();
        }

        final Named bindingName = getBindingName( qualifiedType );
        final Class<?>[] types = getBindingTypes( qualifiedType );

        if ( null != types )
        {
            final Key key = getBindingKey( OBJECT_TYPE_LITERAL, bindingName );
            for ( final Class bindingType : types )
            {
                binder.bind( key.ofType( bindingType ) ).to( qualifiedType );
            }
        }
        else
        {
            binder.bind( new WildcardKey( qualifiedType, bindingName ) ).to( qualifiedType );
        }
    }

    /**
     * Attempts to create a new instance of the given type.
     * 
     * @param type The instance type
     * @return New instance; {@code null} if the instance couldn't be created
     */
    private <T> T newInstance( final Class<T> type )
    {
        try
        {
            // slightly roundabout approach, but it might be private
            final Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible( true );
            return ctor.newInstance();
        }
        catch ( final Exception e )
        {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            binder.addError( "Error creating instance of: " + type + " reason: " + cause );
            return null;
        }
        catch ( final LinkageError e )
        {
            binder.addError( "Error creating instance of: " + type + " reason: " + e );
            return null;
        }
    }

    /**
     * Resolves the type parameters of a super type based on the given concrete type.
     * 
     * @param type The concrete type
     * @param superType The generic super type
     * @return Resolved super type parameters
     */
    private static TypeLiteral<?>[] getSuperTypeParameters( final Class<?> type, final Class<?> superType )
    {
        return TypeParameters.get( TypeLiteral.get( type ).getSupertype( superType ) );
    }

    private static <T> Key<T> getBindingKey( final TypeLiteral<T> bindingType, final Annotation qualifier )
    {
        return null != qualifier ? Key.get( bindingType, qualifier ) : Key.get( bindingType );
    }

    private static Named getBindingName( final Class<?> qualifiedType )
    {
        final javax.inject.Named jsr330 = qualifiedType.getAnnotation( javax.inject.Named.class );
        if ( null != jsr330 )
        {
            try
            {
                final String name = jsr330.value();
                if ( name.length() > 0 )
                {
                    return "default".equals( name ) ? null : Names.named( name );
                }
            }
            catch ( final IncompleteAnnotationException e ) // NOPMD
            {
                // early prototypes of JSR330 @Named declared no default value
            }
        }
        else
        {
            final Named guice = qualifiedType.getAnnotation( Named.class );
            if ( null != guice )
            {
                final String name = guice.value();
                if ( name.length() > 0 )
                {
                    return "default".equals( name ) ? null : guice;
                }
            }
        }

        if ( qualifiedType.getSimpleName().startsWith( "Default" ) )
        {
            return null;
        }

        return Names.named( qualifiedType.getName() );
    }

    private static Class<?>[] getBindingTypes( final Class<?> clazz )
    {
        for ( Class<?> c = clazz; c != Object.class; c = c.getSuperclass() )
        {
            final Typed typed = c.getAnnotation( Typed.class );
            if ( null != typed )
            {
                return typed.value().length > 0 ? typed.value() : c.getInterfaces();
            }
        }
        return null;
    }
}
