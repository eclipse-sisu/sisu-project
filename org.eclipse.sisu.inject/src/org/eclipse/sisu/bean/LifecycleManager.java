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
package org.eclipse.sisu.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.spi.InjectionListener;

final class LifecycleManager
    extends BeanScheduler
    implements InjectionListener<Object>, BeanDisposer
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanStarter starter;

    private final BeanStopper stopper;

    private List<Object> stoppableBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    LifecycleManager( final BeanStarter starter, final BeanStopper stopper )
    {
        this.starter = starter;
        this.stopper = stopper;

        if ( null != stopper )
        {
            stoppableBeans = new ArrayList<Object>();
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void afterInjection( final Object bean )
    {
        if ( null != stopper )
        {
            pushStoppable( bean );
        }
        if ( null != starter )
        {
            schedule( bean );
        }
    }

    @Override
    public void activate( final Object bean )
    {
        starter.start( bean );
    }

    public void dispose( final Object bean )
    {
        if ( removeStoppable( bean ) )
        {
            stopper.stop( bean );
        }
    }

    public void dispose()
    {
        for ( Object bean; ( bean = popStoppable() ) != null; )
        {
            stopper.stop( bean );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private synchronized void pushStoppable( final Object bean )
    {
        stoppableBeans.add( bean );
    }

    private synchronized boolean removeStoppable( final Object bean )
    {
        return stoppableBeans.remove( bean );
    }

    private synchronized Object popStoppable()
    {
        final int size = stoppableBeans.size();
        return size > 0 ? stoppableBeans.remove( size - 1 ) : null;
    }
}
