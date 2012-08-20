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
package org.eclipse.sisu.plexus.binders;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.plexus.binders.PlexusAnnotatedBeanModule;
import org.eclipse.sisu.plexus.binders.PlexusBindingModule;
import org.eclipse.sisu.plexus.config.PlexusBeanConverter;
import org.eclipse.sisu.plexus.config.PlexusBeanLocator;
import org.eclipse.sisu.plexus.converters.PlexusXmlBeanConverter;
import org.eclipse.sisu.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

public class SimpleRequirementExample
{
    public SimpleRequirementExample()
    {
        final String requirement = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
                install( new PlexusBindingModule( null, new PlexusAnnotatedBeanModule( null, null ) ) );
                bindConstant().annotatedWith( Names.named( "example" ) ).to( "TEST" );
            }
        } ).getInstance( Bean.class ).requirement;

        if ( !requirement.equals( "TEST" ) )
        {
            throw new AssertionError();
        }
    }

    @Component( role = Bean.class )
    static class Bean
    {
        @Requirement( hint = "example" )
        String requirement;
    }
}
