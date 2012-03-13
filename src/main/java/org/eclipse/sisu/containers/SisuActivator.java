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
package org.eclipse.sisu.containers;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.binders.ParameterKeys;
import org.eclipse.sisu.binders.SpaceModule;
import org.eclipse.sisu.binders.WireModule;
import org.eclipse.sisu.locators.DefaultBeanLocator;
import org.eclipse.sisu.locators.MutableBeanLocator;
import org.eclipse.sisu.reflect.BundleClassSpace;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.Logs;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * {@link BundleActivator} that maintains a dynamic {@link Injector} graph by scanning bundles as they come and go.
 */
public final class SisuActivator
    implements BundleActivator, BundleTrackerCustomizer, ServiceTrackerCustomizer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String CONTAINER_SYMBOLIC_NAME = "org.eclipse.sisu";

    static final String BUNDLE_INJECTOR_CLASS_NAME = BundleInjector.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    static final MutableBeanLocator locator = new DefaultBeanLocator();

    private BundleContext bundleContext;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        bundleContext = context;
        serviceTracker = new ServiceTracker( context, BUNDLE_INJECTOR_CLASS_NAME, this );
        serviceTracker.open();
        bundleTracker = new BundleTracker( context, Bundle.STARTING | Bundle.ACTIVE, this );
        bundleTracker.open();
    }

    public void stop( final BundleContext context )
    {
        bundleTracker.close();
        serviceTracker.close();
        locator.clear();
    }

    // ----------------------------------------------------------------------
    // Bundle tracking
    // ----------------------------------------------------------------------

    public Object addingBundle( final Bundle bundle, final BundleEvent event )
    {
        if ( CONTAINER_SYMBOLIC_NAME.equals( bundle.getSymbolicName() ) )
        {
            return null; // this is our container, ignore it to avoid circularity errors
        }
        if ( needsScanning( bundle ) && getBundleInjectorService( bundle ) == null )
        {
            try
            {
                new BundleInjector( bundle );
            }
            catch ( final RuntimeException e )
            {
                Logs.warn( "Problem starting: {}", bundle, e );
            }
        }
        return null;
    }

    public void modifiedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        // nothing to do
    }

    public void removedBundle( final Bundle bundle, final BundleEvent event, final Object object )
    {
        // nothing to do
    }

    // ----------------------------------------------------------------------
    // Service tracking
    // ----------------------------------------------------------------------

    @SuppressWarnings( "deprecation" )
    public Object addingService( final ServiceReference reference )
    {
        final Object service = bundleContext.getService( reference );
        locator.add( ( (BundleInjector) service ).getInjector(), 0 );
        return service;
    }

    public void modifiedService( final ServiceReference reference, final Object service )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object service )
    {
        locator.remove( ( (BundleInjector) service ).getInjector() );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static boolean needsScanning( final Bundle bundle )
    {
        final Dictionary<?, ?> headers = bundle.getHeaders();
        final String host = (String) headers.get( Constants.FRAGMENT_HOST );
        if ( null != host )
        {
            return false; // fragment, we'll scan it when we process the host
        }
        final String imports = (String) headers.get( Constants.IMPORT_PACKAGE );
        if ( null == imports )
        {
            return false; // doesn't import any interesting injection packages
        }
        return imports.contains( "javax.inject" ) || imports.contains( "com.google.inject" );
    }

    private static ServiceReference getBundleInjectorService( final Bundle bundle )
    {
        final ServiceReference[] serviceReferences = bundle.getRegisteredServices();
        if ( null != serviceReferences )
        {
            for ( final ServiceReference ref : serviceReferences )
            {
                for ( final String name : (String[]) ref.getProperty( Constants.OBJECTCLASS ) )
                {
                    if ( BUNDLE_INJECTOR_CLASS_NAME.equals( name ) )
                    {
                        return ref;
                    }
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class BundleInjector
        implements /* TODO:ManagedService, */Module
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String[] API = { BUNDLE_INJECTOR_CLASS_NAME /* TODO:, ManagedService.class.getName() */};

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Map<?, ?> properties;

        private final Injector injector;

        private final BundleContext extendedBundleContext;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleInjector( final Bundle bundle )
        {
            extendedBundleContext = bundle.getBundleContext();
            properties = new BundleProperties( extendedBundleContext );

            final ClassSpace space = new BundleClassSpace( bundle );
            final BeanScanning scanning = Main.selectScanning( properties );

            injector = Guice.createInjector( new WireModule( this, new SpaceModule( space, scanning ) ) );

            final Dictionary<Object, Object> metadata = new Hashtable<Object, Object>();
            metadata.put( Constants.SERVICE_PID, CONTAINER_SYMBOLIC_NAME );
            extendedBundleContext.registerService( API, this, metadata );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public void configure( final Binder binder )
        {
            binder.requestStaticInjection( SisuGuice.class );
            binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );
            binder.bind( MutableBeanLocator.class ).toInstance( locator );
            binder.bind( BundleContext.class ).toInstance( extendedBundleContext );
        }

        public Injector getInjector()
        {
            return injector;
        }
    }

    private static final class BundleProperties
        extends AbstractMap<Object, Object>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private transient final BundleContext context;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleProperties( final BundleContext context )
        {
            this.context = context;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        @Override
        public Object get( final Object key )
        {
            return context.getProperty( String.valueOf( key ) );
        }

        @Override
        public boolean containsKey( final Object key )
        {
            return null != get( key );
        }

        @Override
        public Set<Entry<Object, Object>> entrySet()
        {
            return Collections.emptySet();
        }

        @Override
        public int size()
        {
            return 0;
        }
    }
}
