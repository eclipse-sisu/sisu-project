/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *
 * Minimal facade required to be binary-compatible with legacy Plexus API
 *******************************************************************************/
package org.codehaus.plexus.component.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.sisu.plexus.Hints;
import org.eclipse.sisu.plexus.Strategies;

public class ComponentDescriptor<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String role;

    private String hint = Hints.DEFAULT_HINT;

    private String description = "";

    private String instantiationStrategy = Strategies.SINGLETON;

    private String implementation;

    private ClassRealm classRealm;

    private Class<?> implementationClass;

    private String componentComposer;

    private String componentConfigurator;

    private String componentFactory;

    private List<ComponentRequirement> requirements = Collections.emptyList();

    private PlexusConfiguration configuration;

    private String alias;

    private String version;

    private String componentType;

    private String componentProfile;

    private String lifecycleHandler;

    private boolean isolatedRealm;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentDescriptor()
    {
        // nothing to set
    }

    public ComponentDescriptor( final Class<T> implementationClass, final ClassRealm classRealm )
    {
        setImplementationClass( implementationClass );
        this.classRealm = classRealm;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void setRole( final String role )
    {
        this.role = role;
    }

    public final void setRoleClass( final Class<?> roleClass )
    {
        role = roleClass.getName();
    }

    public final void setRoleHint( final String hint )
    {
        this.hint = Hints.canonicalHint( hint );
    }

    public final void setDescription( final String description )
    {
        this.description = null != description ? description : "";
    }

    public final void setInstantiationStrategy( final String instantiationStrategy )
    {
        this.instantiationStrategy = instantiationStrategy;
    }

    public final void setImplementation( final String implementation )
    {
        this.implementation = implementation;
        implementationClass = null;
    }

    public final void setRealm( final ClassRealm classRealm )
    {
        this.classRealm = classRealm;
        implementationClass = null;
    }

    @SuppressWarnings( "rawtypes" )
    public final void setImplementationClass( final Class implementationClass )
    {
        this.implementationClass = implementationClass;
        implementation = implementationClass.getName();
    }

    public final void setComponentComposer( final String componentComposer )
    {
        this.componentComposer = componentComposer;
    }

    public final void setComponentConfigurator( final String componentConfigurator )
    {
        this.componentConfigurator = componentConfigurator;
    }

    public final void setComponentFactory( final String componentFactory )
    {
        this.componentFactory = componentFactory;
    }

    public final void addRequirement( final ComponentRequirement requirement )
    {
        if ( requirements.isEmpty() )
        {
            requirements = new ArrayList<ComponentRequirement>();
        }
        requirements.add( requirement );
    }

    public final void setConfiguration( final PlexusConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public final void setAlias( final String alias )
    {
        this.alias = alias;
    }

    public final void setVersion( final String version )
    {
        this.version = version;
    }

    public final void setComponentType( final String componentType )
    {
        this.componentType = componentType;
    }

    public final void setComponentProfile( final String componentProfile )
    {
        this.componentProfile = componentProfile;
    }

    public final void setLifecycleHandler( final String lifecycleHandler )
    {
        this.lifecycleHandler = lifecycleHandler;
    }

    public final void setIsolatedRealm( final boolean isolatedRealm )
    {
        this.isolatedRealm = isolatedRealm;
    }

    public String getRole()
    {
        return role;
    }

    @SuppressWarnings( "unchecked" )
    public final Class<T> getRoleClass()
    {
        try
        {
            return (Class<T>) classRealm.loadClass( getRole() );
        }
        catch ( final Exception e )
        {
            throw new TypeNotPresentException( getRole(), e );
        }
        catch ( final LinkageError e )
        {
            throw new TypeNotPresentException( getRole(), e );
        }
    }

    public String getRoleHint()
    {
        return hint;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getInstantiationStrategy()
    {
        return instantiationStrategy;
    }

    public final String getImplementation()
    {
        return implementation;
    }

    public final ClassRealm getRealm()
    {
        return classRealm;
    }

    @SuppressWarnings( "unchecked" )
    public final Class<T> getImplementationClass()
    {
        if ( null == implementationClass && null != classRealm )
        {
            try
            {
                implementationClass = classRealm.loadClass( implementation );
            }
            catch ( final Exception e )
            {
                throw new TypeNotPresentException( implementation, e );
            }
            catch ( final LinkageError e )
            {
                throw new TypeNotPresentException( implementation, e );
            }
        }
        return (Class<T>) implementationClass;
    }

    public final String getComponentComposer()
    {
        return componentComposer;
    }

    public final String getComponentConfigurator()
    {
        return componentConfigurator;
    }

    public final String getComponentFactory()
    {
        return componentFactory;
    }

    public final List<ComponentRequirement> getRequirements()
    {
        return Collections.unmodifiableList( requirements );
    }

    public final boolean hasConfiguration()
    {
        return configuration != null;
    }

    public final PlexusConfiguration getConfiguration()
    {
        return configuration;
    }

    public final String getAlias()
    {
        return alias;
    }

    public final String getVersion()
    {
        return version;
    }

    public String getComponentType()
    {
        return componentType;
    }

    public final String getComponentProfile()
    {
        return componentProfile;
    }

    public final String getLifecycleHandler()
    {
        return lifecycleHandler;
    }

    public final boolean isIsolatedRealm()
    {
        return isolatedRealm;
    }

    public final String getHumanReadableKey()
    {
        return "role: '" + getRole() + "', implementation: '" + implementation + "', role hint: '" + getRoleHint()
            + "'";
    }

    @Override
    public final String toString()
    {
        return getClass().getName() + " [role: '" + getRole() + "', hint: '" + getRoleHint() + "', realm: " + classRealm
            + "]";
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof ComponentDescriptor<?> )
        {
            final ComponentDescriptor<?> rhsDescriptor = (ComponentDescriptor<?>) rhs;
            return equals( getRole(), rhsDescriptor.getRole() ) && equals( getRoleHint(), rhsDescriptor.getRoleHint() )
                && equals( classRealm, rhsDescriptor.classRealm );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return ( ( 17 * 31 + hash( getRole() ) ) * 31 + hash( getRoleHint() ) ) * 31 + hash( classRealm );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final <T> boolean equals( final T lhs, final T rhs )
    {
        return null != lhs ? lhs.equals( rhs ) : null == rhs;
    }

    private static final int hash( final Object obj )
    {
        return null != obj ? obj.hashCode() : 0;
    }
}
