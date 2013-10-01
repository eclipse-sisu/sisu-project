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
package org.eclipse.sisu.osgi.extender;

import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.launch.SisuExtensions;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.wire.ParameterKeys;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;

/**
 * A Guice module backed by a bundle that is created any time a bundle comes online and is picked by the extender.
 * <p>
 * {@link BundleModule} instances are created by {@link ModuleBundleTracker} whenever a new bundle comes online that is
 * considered a potential consumer or provider of injectable types.
 * </p>
 * 
 * @see ModuleBundleTracker
 * @see {@link SisuExtensions}.
 */
public class BundleModule
    extends AbstractModule
{

    private final Bundle bundle;

    private final BeanLocator locator;

    private final ClassSpace space;

    private final SisuExtensions extensions;

    /**
     * Creates a new instance given the shared bean locator and the new bundle.
     * 
     * @param locator the bean locator
     * @param bundle the new bundle
     */
    public BundleModule( BeanLocator locator, Bundle bundle )
    {
        this.locator = locator;
        this.bundle = bundle;
        this.space = new BundleClassSpace( bundle );
        this.extensions = SisuExtensions.local( space );
    }

    /**
     * Returns the {@link Bundle} of this bundle module.
     * @return the bundle
     */
    public Bundle getBundle()
    {
        return bundle;
    }
    
    /**
     * Returns the {@link ClassSpace} instance created for the given bundle.
     * 
     * @return the class space
     */
    public ClassSpace getSpace()
    {
        return space;
    }

    /**
     * Return the {@link SisuExtensions} instance created for the given bundle.
     * 
     * @return the sisu extensions
     */
    public SisuExtensions getExtensions()
    {
        return extensions;
    }

    /*
     * (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure()
    {
        BundleContext bundleContext = bundle.getBundleContext();
        bind( BeanLocator.class ).toInstance( locator );
        bind( BundleContext.class ).toInstance( bundleContext );
        bind( ParameterKeys.PROPERTIES ).toInstance( System.getProperties() );
        extensions.install( binder(), Bundle.class, bundle );
    }
}
