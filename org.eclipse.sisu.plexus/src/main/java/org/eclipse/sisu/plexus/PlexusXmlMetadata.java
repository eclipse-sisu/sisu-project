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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.bean.BeanProperty;

/**
 * Consumable {@link PlexusBeanMetadata} that uses {@link BeanProperty} names as keys.
 */
final class PlexusXmlMetadata
    implements PlexusBeanMetadata
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private Map<String, Configuration> configurationMap = Collections.emptyMap();

    private Map<String, Requirement> requirementMap = Collections.emptyMap();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusXmlMetadata( final Map<String, Configuration> configurationMap,
                       final Map<String, Requirement> requirementMap )
    {
        merge( configurationMap, requirementMap );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean isEmpty()
    {
        return configurationMap.isEmpty() && requirementMap.isEmpty();
    }

    public Configuration getConfiguration( final BeanProperty<?> property )
    {
        return configurationMap.remove( property.getName() );
    }

    public Requirement getRequirement( final BeanProperty<?> property )
    {
        Requirement requirement = requirementMap.remove( property.getName() );
        if ( null == requirement )
        {
            // perhaps requirement uses the fully-qualified role name (see PlexusXmlScanner)
            requirement = requirementMap.remove( property.getType().getRawType().getName() );
        }
        return requirement;
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * Merges the given configuration and requirements with the current metadata, without overwriting existing entries.
     * 
     * @param extraConfiguration The extra configuration
     * @param extraRequirements The extra requirements
     */
    void merge( final Map<String, Configuration> extraConfiguration, final Map<String, Requirement> extraRequirements )
    {
        configurationMap = addIfMissing( configurationMap, extraConfiguration );
        requirementMap = addIfMissing( requirementMap, extraRequirements );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Looks for keys that exist in the secondary map, but not the primary, and adds their mappings to the primary map.
     * 
     * @param primary The primary map
     * @param secondary The secondary map
     */
    private static <K, V> Map<K, V> addIfMissing( final Map<K, V> primary, final Map<K, V> secondary )
    {
        // nothing to add?
        if ( secondary.isEmpty() )
        {
            return primary;
        }

        // nothing to merge?
        if ( primary.isEmpty() )
        {
            return secondary;
        }

        // primary mappings always override secondary mappings
        final Map<K, V> tempMap = new HashMap<K, V>( secondary );
        tempMap.putAll( primary );
        return tempMap;
    }
}
