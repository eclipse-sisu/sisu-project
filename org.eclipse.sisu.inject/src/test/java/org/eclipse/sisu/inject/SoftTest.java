/*
 * Copyright (c) 2010-2024 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 */
package org.eclipse.sisu.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class SoftTest {
    @Test
    void testSoftElements() {
        final Collection<String> elements = Soft.elements();
        assertNotNull(elements);
        assertTrue(elements.isEmpty());
        elements.add("a");
        elements.add("b");
        assertEquals(2, elements.size());
        assertTrue(elements.contains("a"));
    }

    @Test
    void testSoftElementsWithCapacity() {
        final Collection<String> elements = Soft.elements(5);
        assertNotNull(elements);
        elements.add("x");
        assertEquals(1, elements.size());

        final Iterator<String> iterator = elements.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("x", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testSoftKeys() {
        final Map<String, Integer> map = Soft.keys();
        assertNotNull(map);
        assertTrue(map.isEmpty());
        map.put("one", 1);
        map.put("two", 2);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(1), map.get("one"));
    }

    @Test
    void testSoftKeysWithCapacity() {
        final Map<String, Integer> map = Soft.keys(8);
        assertNotNull(map);
        map.put("a", 1);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("a"));
        map.remove("a");
        assertTrue(map.isEmpty());
    }

    @Test
    void testSoftValues() {
        final Map<String, Integer> map = Soft.values();
        assertNotNull(map);
        assertTrue(map.isEmpty());
        map.put("one", 1);
        map.put("two", 2);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(1), map.get("one"));
    }

    @Test
    void testSoftValuesWithCapacity() {
        final Map<String, Integer> map = Soft.values(8);
        assertNotNull(map);
        map.put("a", 1);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("a"));
        map.remove("a");
        assertTrue(map.isEmpty());
    }

    @Test
    void testConcurrentKeys() {
        final ConcurrentMap<String, Integer> map = Soft.concurrentKeys();
        assertNotNull(map);
        assertTrue(map.isEmpty());
        map.put("one", 1);
        assertEquals(Integer.valueOf(1), map.get("one"));
    }

    @Test
    void testConcurrentKeysWithCapacity() {
        final ConcurrentMap<String, Integer> map = Soft.concurrentKeys(8, 2);
        assertNotNull(map);
        assertNull(map.putIfAbsent("a", 1));
        assertEquals(Integer.valueOf(1), map.putIfAbsent("a", 2)); // key already exists
        assertEquals(Integer.valueOf(1), map.get("a"));
    }

    @Test
    void testConcurrentValues() {
        final ConcurrentMap<String, Integer> map = Soft.concurrentValues();
        assertNotNull(map);
        assertTrue(map.isEmpty());
        map.put("one", 1);
        assertEquals(Integer.valueOf(1), map.get("one"));
    }

    @Test
    void testConcurrentValuesWithCapacity() {
        final ConcurrentMap<String, Integer> map = Soft.concurrentValues(8, 2);
        assertNotNull(map);
        map.put("a", 1);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("a"));
    }
}
