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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.sisu.BeanScanning;
import org.eclipse.sisu.binders.ParameterKeys;
import org.eclipse.sisu.binders.SpaceModule;
import org.eclipse.sisu.binders.WireModule;
import org.eclipse.sisu.locators.MutableBeanLocator;
import org.eclipse.sisu.reflect.ClassSpace;
import org.eclipse.sisu.reflect.URLClassSpace;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Abstract JUnit3 {@link TestCase} that automatically binds and injects itself.
 */
public abstract class InjectedTestCase
    extends TestCase
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String basedir;

    @Inject
    private MutableBeanLocator locator;

    // ----------------------------------------------------------------------
    // Setup
    // ----------------------------------------------------------------------

    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new WireModule( new SetUpModule(), new SpaceModule( space(), scanning() ) ) );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        locator.clear();
    }

    final class SetUpModule
        implements Module
    {
        public void configure( final Binder binder )
        {
            binder.install( InjectedTestCase.this );

            final Properties properties = new Properties();
            properties.put( "basedir", getBasedir() );
            InjectedTestCase.this.configure( properties );

            binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );

            binder.requestInjection( InjectedTestCase.this );
        }
    }

    public ClassSpace space()
    {
        return new URLClassSpace( getClass().getClassLoader() );
    }

    public BeanScanning scanning()
    {
        return BeanScanning.CACHE;
    }

    // ----------------------------------------------------------------------
    // Container configuration methods
    // ----------------------------------------------------------------------

    /**
     * Custom injection bindings.
     * 
     * @param binder The Guice binder
     */
    public void configure( final Binder binder )
    {
        // place any per-test bindings here...
    }

    /**
     * Custom property values.
     * 
     * @param properties The test properties
     */
    public void configure( final Properties properties )
    {
        // put any per-test properties here...
    }

    // ----------------------------------------------------------------------
    // Container lookup methods
    // ----------------------------------------------------------------------

    public final <T> T lookup( final Class<T> type )
    {
        return lookup( Key.get( type ) );
    }

    public final <T> T lookup( final Class<T> type, final String name )
    {
        return lookup( type, Names.named( name ) );
    }

    public final <T> T lookup( final Class<T> type, final Class<? extends Annotation> qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    public final <T> T lookup( final Class<T> type, final Annotation qualifier )
    {
        return lookup( Key.get( type, qualifier ) );
    }

    // ----------------------------------------------------------------------
    // Container resource methods
    // ----------------------------------------------------------------------

    public final String getBasedir()
    {
        if ( null == basedir )
        {
            basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
        }
        return basedir;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private final <T> T lookup( final Key<T> key )
    {
        final Iterator<? extends Entry<Annotation, T>> i = locator.locate( key ).iterator();
        return i.hasNext() ? i.next().getValue() : null;
    }
}
