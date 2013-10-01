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
package org.eclipse.sisu.space;

import java.util.Map;

/**
 * Common techniques for discovering bean implementations.
 * 
 * @see org.eclipse.sisu.space.SpaceModule
 */
public enum BeanScanning
{
    /**
     * Always scan
     */
    ON,

    /**
     * Never scan
     */
    OFF,

    /**
     * Scan once and cache results
     */
    CACHE,

    /**
     * Use local index (plug-ins)
     */
    INDEX,

    /**
     * Use global index (application)
     */
    GLOBAL_INDEX;

    /**
     * Selects the {@link BeanScanning} strategy from the given properties map. <br>
     * Checks whether the map contains a property named {@link org.eclipse.sisu.space.BeanScanning} whose value
     * corresponds to the string representation of one of the literals of the {@link BeanScanning} enumeration. If the
     * property is not present in the map, {@link #ON} is selected. If the map contains an invalid value an
     * {@link IllegalAccessException} is thrown.
     * 
     * @param properties the map of properties
     * @return the BeanScanning value
     * @throws IllegalArgumentException if the properties map contains an invalid value
     */
    public static BeanScanning selectScanning( final Map<?, ?> properties )
    {
        final String option = (String) properties.get( BeanScanning.class.getName() );
        if ( null == option || option.length() == 0 )
        {
            return BeanScanning.ON;
        }

        return valueOf( option );
    }

}
