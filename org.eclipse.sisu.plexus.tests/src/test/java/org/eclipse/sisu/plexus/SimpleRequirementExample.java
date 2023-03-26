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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

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
