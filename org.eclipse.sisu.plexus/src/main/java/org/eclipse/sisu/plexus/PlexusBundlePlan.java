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

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.InjectorBindings;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.BundleModule;
import org.eclipse.sisu.launch.BundlePlan;
import org.eclipse.sisu.space.BeanScanning;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.Guice;
import com.google.inject.Module;

/**
 * {@link BundlePlan} that prepares {@link BindingPublisher}s for Plexus bundles.
 */
public class PlexusBundlePlan
    implements BundlePlan
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String SUPPORT_BUNDLE_NAME =
        FrameworkUtil.getBundle( PlexusSpaceModule.class ).getSymbolicName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    protected final MutableBeanLocator locator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBundlePlan( final MutableBeanLocator locator )
    {
        this.locator = locator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public BindingPublisher prepare( final Bundle bundle )
    {
        if ( !SUPPORT_BUNDLE_NAME.equals( bundle.getSymbolicName() ) )
        {
            if ( hasPlexusAnnotations( bundle ) || hasPlexusXml( bundle ) )
            {
                return new InjectorBindings( Guice.createInjector( new BundleModule( bundle, locator )
                {
                    @Override
                    protected Module spaceModule()
                    {
                        return new PlexusSpaceModule( space, BeanScanning.select( getProperties() ) );
                    }
                } ) );
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    protected static boolean hasPlexusAnnotations( final Bundle bundle )
    {
        final String imports = bundle.getHeaders().get( Constants.IMPORT_PACKAGE );
        return null != imports && imports.contains( "org.codehaus.plexus.component.annotations" );
    }

    protected static boolean hasPlexusXml( final Bundle bundle )
    {
        return null != bundle.findEntries( "META-INF/plexus", "components.xml", false );
    }
}
