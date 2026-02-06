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
package org.eclipse.sisu.wire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class EntryMapAdapterTest {
    @SuppressWarnings("boxing")
    @Test
    void testMapSize() {
        final Map<String, Integer> original = new HashMap<>();
        final Map<String, Integer> adapter = new EntryMapAdapter<>(original.entrySet());

        assertTrue(adapter.isEmpty());
        original.put("A", 1);
        assertFalse(adapter.isEmpty());

        assertEquals(1, adapter.size());
        original.put("C", 3);
        assertEquals(2, adapter.size());
        original.put("B", 2);
        assertEquals(3, adapter.size());
        original.remove("C");
        assertEquals(2, adapter.size());
    }

    @SuppressWarnings("boxing")
    @Test
    void testMapEquality() {
        final Map<Integer, String> original = new HashMap<>();
        final Map<Integer, String> adapter = new EntryMapAdapter<>(original.entrySet());

        assertEquals(original, adapter);
        original.put(3, "C");
        assertEquals(original, adapter);
        original.put(1, "A");
        assertEquals(original, adapter);
        original.put(2, "B");
        assertEquals(original, adapter);
        original.clear();
        assertEquals(original, adapter);
    }
}
