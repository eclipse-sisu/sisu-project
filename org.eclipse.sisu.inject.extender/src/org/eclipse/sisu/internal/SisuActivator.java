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
package org.eclipse.sisu.internal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.Logs;
import org.eclipse.sisu.launch.Main;
import org.eclipse.sisu.launch.SisuExtensions;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * {@link BundleActivator} that maintains a dynamic {@link Injector} graph by scanning bundles as they come and go.
 */
public final class SisuActivator
    implements BundleActivator, BundleTrackerCustomizer, ServiceTrackerCustomizer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String CONTAINER_SYMBOLIC_NAME = "org.eclipse.sisu.inject";

    static final String BUNDLE_INJECTOR_CLASS_NAME = BundleInjector.class.getName();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static Reference<DefaultBeanLocator> LOCATOR_REF;

    private DefaultBeanLocator locator;

    private BundleContext sisuBundleContext;

    private ServiceTracker serviceTracker;

    private BundleTracker bundleTracker;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start( final BundleContext context )
    {
        synchronized ( SisuActivator.class )
        {
            if ( null != LOCATOR_REF )
            {
                locator = LOCATOR_REF.get();
            }
            if ( null == locator )
            {
                LOCATOR_REF = new WeakReference<DefaultBeanLocator>( locator = new DefaultBeanLocator() );
            }
        }

        sisuBundleContext = context;
        serviceTracker = new ServiceTracker( context, BUNDLE_INJECTOR_CLASS_NAME, this );
        serviceTracker.open( true );
        bundleTracker = new BundleTracker( context, Bundle.ACTIVE, this );
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
                new BundleInjector( locator, bundle );
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

    public Object addingService( final ServiceReference reference )
    {
        final Object service = sisuBundleContext.getService( reference );
        if ( service instanceof Provider )
        {
            final Object injector = ( (Provider<?>) service ).get();
            if ( injector instanceof Injector )
            {
                locator.add( (Injector) injector, 0 );
                return injector;
            }
        }
        return null;
    }

    public void modifiedService( final ServiceReference reference, final Object injector )
    {
        // nothing to do
    }

    public void removedService( final ServiceReference reference, final Object injector )
    {
        if ( injector instanceof Injector )
        {
            locator.remove( (Injector) injector );
        }
        sisuBundleContext.ungetService( reference );
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
        implements Provider<Injector>/* TODO: ManagedService */
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String[] API = { BUNDLE_INJECTOR_CLASS_NAME /* TODO:, ManagedService.class.getName() */};

        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Injector injector;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BundleInjector( final DefaultBeanLocator locator, final Bundle bundle )
        {
            final BundleContext extendedBundleContext = bundle.getBundleContext();
            final Map<?, ?> properties = new BundleProperties( extendedBundleContext );

            final ClassSpace space = new BundleClassSpace( bundle );
            final BeanScanning scanning = Main.selectScanning( properties );
            final SisuExtensions extensions = SisuExtensions.local( space );

            injector = Guice.createInjector( new WireModule( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( DefaultBeanLocator.class ).toInstance( locator );
                    bind( BundleContext.class ).toInstance( extendedBundleContext );
                    bind( ParameterKeys.PROPERTIES ).toInstance( properties );

                    extensions.install( binder(), Bundle.class, bundle );
                }
            }, new SpaceModule( space, scanning ).with( extensions ) ).with( extensions ) );

            final Dictionary<Object, Object> metadata = new Hashtable<Object, Object>();
            metadata.put( Constants.SERVICE_PID, CONTAINER_SYMBOLIC_NAME );
            extendedBundleContext.registerService( API, this, metadata );
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public Injector get()
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

        private final transient BundleContext context;

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
