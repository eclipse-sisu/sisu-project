/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.factory.ComponentFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.eclipse.sisu.bean.BeanProperty;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.inject.DeferredProvider;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.LoadedClass;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

/**
 * {@link PlexusBeanModule} that binds Plexus components according to their {@link ComponentDescriptor}s.
 */
public final class ComponentDescriptorBeanModule
    implements PlexusBeanModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final Map<Component, DeferredClass<?>> componentMap = new HashMap<Component, DeferredClass<?>>();

    private final Map<String, PlexusBeanMetadata> metadataMap = new HashMap<String, PlexusBeanMetadata>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentDescriptorBeanModule( final ClassSpace space, final List<ComponentDescriptor<?>> descriptors )
    {
        this.space = space;

        for ( int i = 0, size = descriptors.size(); i < size; i++ )
        {
            final ComponentDescriptor<?> cd = descriptors.get( i );
            final Component component = newComponent( cd );
            final String factory = cd.getComponentFactory();
            if ( null == factory || "java".equals( factory ) )
            {
                try
                {
                    componentMap.put( component, new LoadedClass<Object>( cd.getImplementationClass() ) );
                }
                catch ( final TypeNotPresentException e )
                {
                    componentMap.put( component, space.deferLoadClass( cd.getImplementation() ) );
                }
            }
            else
            {
                componentMap.put( component, new DeferredFactoryClass( cd, factory ) );
            }
            final List<ComponentRequirement> requirements = cd.getRequirements();
            if ( !requirements.isEmpty() )
            {
                metadataMap.put( cd.getImplementation(), new ComponentMetadata( space, requirements ) );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public PlexusBeanSource configure( final Binder binder )
    {
        final String source = space.toString();
        final PlexusTypeBinder plexusTypeBinder = new PlexusTypeBinder( binder );
        for ( final Entry<Component, DeferredClass<?>> entry : componentMap.entrySet() )
        {
            plexusTypeBinder.hear( entry.getKey(), entry.getValue(), source );
        }
        return new PlexusDescriptorBeanSource( metadataMap );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    static Component newComponent( final ComponentDescriptor<?> cd )
    {
        return new ComponentImpl( cd.getRoleClass(), cd.getRoleHint(), cd.getInstantiationStrategy(),
                                  cd.getDescription() );
    }

    static Requirement newRequirement( final ClassSpace space, final ComponentRequirement cr )
    {
        return new RequirementImpl( space.deferLoadClass( cr.getRole() ), cr.isOptional(),
                                    Collections.singletonList( cr.getRoleHint() ) );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link DeferredClass} backed by a {@link ComponentDescriptor} and {@link ComponentFactory} hint.
     */
    private static final class DeferredFactoryClass
        implements DeferredClass<Object>, DeferredProvider<Object>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        @Inject
        private PlexusContainer container;

        @Inject
        private Injector injector;

        private final ComponentDescriptor<?> cd;

        private final String hint;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        DeferredFactoryClass( final ComponentDescriptor<?> cd, final String hint )
        {
            this.cd = cd;
            this.hint = hint;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @SuppressWarnings( { "unchecked", "rawtypes" } )
        public Class load()
            throws TypeNotPresentException
        {
            return cd.getImplementationClass();
        }

        public String getName()
        {
            return cd.getImplementation();
        }

        public DeferredProvider<Object> asProvider()
        {
            return this;
        }

        public Object get()
        {
            try
            {
                ClassRealm contextRealm = container.getLookupRealm();
                if ( null == contextRealm )
                {
                    contextRealm = RealmManager.contextRealm();
                }
                if ( null == contextRealm )
                {
                    contextRealm = container.getContainerRealm();
                }
                final ComponentFactory factory = container.lookup( ComponentFactory.class, hint );
                final Object o = factory.newInstance( cd, contextRealm, container );
                if ( null != o )
                {
                    injector.injectMembers( o );
                }
                return o;
            }
            catch ( final Exception e )
            {
                throw new ProvisionException( "Error in ComponentFactory:" + hint, e );
            }
        }

        public DeferredClass<Object> getImplementationClass()
        {
            return this;
        }
    }

    /**
     * {@link PlexusBeanMetadata} backed by list of {@link ComponentRequirement}s.
     */
    private static final class ComponentMetadata
        implements PlexusBeanMetadata
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Map<String, Requirement> requirementMap = new HashMap<String, Requirement>();

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ComponentMetadata( final ClassSpace space, final List<ComponentRequirement> requirements )
        {
            for ( int i = 0, size = requirements.size(); i < size; i++ )
            {
                final ComponentRequirement cr = requirements.get( i );
                requirementMap.put( cr.getFieldName(), newRequirement( space, cr ) );
            }
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean isEmpty()
        {
            return requirementMap.isEmpty();
        }

        public Requirement getRequirement( final BeanProperty<?> property )
        {
            final Requirement requirement = requirementMap.get( property.getName() );
            if ( null != requirement && requirementMap.isEmpty() )
            {
                requirementMap = Collections.emptyMap();
            }
            return requirement;
        }

        public Configuration getConfiguration( final BeanProperty<?> property )
        {
            return null;
        }
    }

    /**
     * {@link PlexusBeanSource} backed by simple map of {@link PlexusBeanMetadata}.
     */
    private static final class PlexusDescriptorBeanSource
        implements PlexusBeanSource
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private Map<String, PlexusBeanMetadata> metadataMap;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        PlexusDescriptorBeanSource( final Map<String, PlexusBeanMetadata> metadataMap )
        {
            this.metadataMap = metadataMap;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( null == metadataMap )
            {
                return null;
            }
            final PlexusBeanMetadata metadata = metadataMap.remove( implementation.getName() );
            if ( metadataMap.isEmpty() )
            {
                metadataMap = null;
            }
            return metadata;
        }
    }
}
