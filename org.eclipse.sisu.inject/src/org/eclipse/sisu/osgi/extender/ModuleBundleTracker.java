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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.SisuExtensions;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * {@link BundleTrackerCustomizer} used to track bundles as they come and go. <br>
 * {@link ModuleBundleTracker} uses {@link BundleSelector} to decide whether a tracked bundle is relevant for Sisu or
 * not. <br>
 * <br>
 * When a bundle is considered relevant for Sisu, a new {@link BindingPublisher} is created for the new bundle. More
 * precisely, an {@link InjectorPublisher} is instantiated passing in an {@link Injector} created for the new bundle
 * that uses a shared {@link MutableBeanLocator} instance. The newly created {@link BindingPublisher} is then registered
 * in the OSGi service registry. <br>
 * Finally, the {@link PublisherServiceTracker} uses the newly registered service to extend Sisu. <br/>
 * <br/>
 * The {@link BindingPublisher} instance is registered in the registry using the following meta-data:
 * <ul>
 * <li>service.pid = "org.eclipse.sisu.inject" (string value)</li>
 * <li>org.eclipse.sisu.inject.extender = "true" (string value)</li>
 * </ul>
 * <code>service.pid</code> is used by Sisu to ensure that it will select those instances of {@link BindingPublisher}
 * found in the OSGi registry that have been created specifically for Sisu. This filtering policy allows user code to
 * bind into the OSGi service registry additional {@link BindingPublisher} instances for own use that will not be
 * considered by Sisu. <br>
 * <code>org.eclipse.sisu.inject.extender</code> property is reserved for use by the Sisu extender only and should be
 * set to true only for {@link BindingPublisher} services that have been registered by the extender itself. This allows
 * the extender to distinguish between a service registered for Sisu by the extender and a service registered for Sisu
 * by some user code.
 */
public class ModuleBundleTracker
    implements BundleTrackerCustomizer
{

    /* package */final static String SYMBOLIC_NAME =
        FrameworkUtil.getBundle( ModuleBundleTracker.class ).getSymbolicName();

    private final static String REGISTERED_BY_EXTENDER_PROPERTY = SYMBOLIC_NAME + ".extender";

    private final BundleSelector selector;

    private final MutableBeanLocator locator;

    /**
     * Creates a new instance given the shared bean locator.
     * 
     * @param locator the shared bean locator.
     */
    public ModuleBundleTracker( MutableBeanLocator locator )
    {
        this.locator = locator;
        this.selector = createSelector();
    }

    /**
     * Creates the {@link BundleSelector}.
     * 
     * @see BundleSelector
     * @return the {@link BundleSelector} in use.
     */
    protected BundleSelector createSelector()
    {
        BundleSelector tmpSelector = null;
        String className = System.getProperty( BundleSelector.class.getName(), DefaultBundleSelector.class.getName() );
        try
        {
            Class maybeSelector = getClass().getClassLoader().loadClass( className );
            if ( BundleSelector.class.isAssignableFrom( maybeSelector ) )
            {
                tmpSelector = (BundleSelector) maybeSelector.newInstance();
            }
        }
        catch ( Exception e )
        {
            Logs.warn( "Invalid BundleSelector option: {}", className, e );
        }

        if ( null == tmpSelector )
        {
            tmpSelector = new DefaultBundleSelector();
        }

        return tmpSelector;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     * org.osgi.framework.BundleEvent)
     */
    public Object addingBundle( Bundle bundle, BundleEvent event )
    {
        if ( SYMBOLIC_NAME.equals( bundle.getSymbolicName() ) )
        {
            return null; // this is Sisu, ignore it to avoid
                         // circularity errors
        }
        if ( needsScanning( bundle ) && !isBindingPublisherRegistered( bundle ) )
        {
            try
            {
                BindingPublisher publisher = createBindingPublisher( bundle );

                Dictionary<String, Object> metadata = createServiceMetadata( bundle );
                bundle.getBundleContext().registerService( BindingPublisher.class.getName(), publisher, metadata );

            }
            catch ( RuntimeException e )
            {
                Logs.warn( "Problem starting: {}", bundle, e );
            }
        }
        return null;
    }

    /**
     * Creates the {@link BindingPublisher} that is registered in the OSGi registry. Default implementation returns an
     * instance of {@link InjectorPublisher}.
     * 
     * @param bundle the extended bundle.
     * @return the {@link BindingPublisher} instance.
     */
    protected BindingPublisher createBindingPublisher( Bundle bundle )
    {
        BundleModule module = new BundleModule( locator, bundle );
        ClassSpace space = module.getSpace();
        SisuExtensions extensions = module.getExtensions();

        BeanScanning scanning = createScanning( module );

        Injector injector =
            Guice.createInjector( new WireModule( module, new SpaceModule( space, scanning ).with( extensions ) ).with( extensions ) );

        return new InjectorPublisher( injector, new DefaultRankingFunction() );
    }

    /**
     * Creates the {@link BeanScanning} strategy for the given module.
     * 
     * @param module the {@link BundleModule}
     * @return
     */
    protected BeanScanning createScanning( BundleModule module )
    {
        return BeanScanning.selectScanning( System.getProperties() );
    }

    /**
     * Creates the meta-data that is passed to the OSGi registry during the registration of the {@link BindingPublisher}
     * service. See class documentation for details about the meta-data.
     * 
     * @param bundle the extended bundle
     * @return the meta-data
     */
    protected Dictionary<String, Object> createServiceMetadata( Bundle bundle )
    {
        Dictionary<String, Object> metadata = new Hashtable<String, Object>();
        metadata.put( Constants.SERVICE_PID, SYMBOLIC_NAME );
        metadata.put( REGISTERED_BY_EXTENDER_PROPERTY, Boolean.TRUE.toString() );
        return metadata;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     * org.osgi.framework.BundleEvent, java.lang.Object)
     */
    public void modifiedBundle( Bundle bundle, BundleEvent event, Object object )
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     * org.osgi.framework.BundleEvent, java.lang.Object)
     */
    public void removedBundle( Bundle bundle, BundleEvent event, Object object )
    {
        /*
         * nothing to do, clean-up is performed by the service tracker when the BundleInjector is removed from the
         * registry by the OSGi framework (because its bundle got removed).
         */

    }

    /**
     * Tells whether a bundle is to be examined by the extender or not.
     * 
     * @param bundle a candidate bundle for the extender
     * @return true if the bundle is to be extended
     */
    protected boolean needsScanning( final Bundle bundle )
    {
        return selector.select( bundle );
    }

    /**
     * Checks whether the {@link PublisherServiceTracker} already registered a {@link BindingPublisher} as an OSGi
     * service or not. Used to decide whether service registration is needed or not.
     * 
     * @param bundle the extended bundle
     * @return true if the service has already been registered, false otherwise.
     */
    protected boolean isBindingPublisherRegistered( Bundle bundle )
    {
        ServiceReference[] serviceReferences = bundle.getRegisteredServices();
        if ( null != serviceReferences )
        {
            for ( final ServiceReference ref : serviceReferences )
            {
                Object pid = ref.getProperty( Constants.SERVICE_PID );
                boolean registeredByExtender =
                    Boolean.parseBoolean( (String) ref.getProperty( REGISTERED_BY_EXTENDER_PROPERTY ) );
                if ( registeredByExtender && SYMBOLIC_NAME.equals( pid ) )
                {
                    for ( final String name : (String[]) ref.getProperty( Constants.OBJECTCLASS ) )
                    {
                        if ( BindingPublisher.class.getName().equals( name ) )
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
