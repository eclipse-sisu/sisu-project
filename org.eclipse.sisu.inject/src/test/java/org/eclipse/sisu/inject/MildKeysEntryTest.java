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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class MildKeysEntryTest {
    @Test
    void testEntrySet() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        int count = 0;
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    void testEntrySetIteratorRemove() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        map.put("x", "10");
        map.put("y", "20");

        final Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();
        assertTrue(entryIterator.hasNext());
        entryIterator.next();
        entryIterator.remove();
        assertEquals(1, map.size());
    }

    @Test
    void testEntrySetNoSuchElement() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        final Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();
        assertFalse(entryIterator.hasNext());
        try {
            entryIterator.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException expected) {
            // expected
        }
    }

    @Test
    void testStrongEntrySetValue() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        map.put("key", "oldValue");

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            assertEquals("key", entry.getKey());
            assertEquals("oldValue", entry.getValue());
            entry.setValue("newValue");
        }
        assertEquals("newValue", map.get("key"));
    }

    @Test
    void testContainsKey() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        assertFalse(map.containsKey("a"));
        map.put("a", "1");
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
    }

    @Test
    void testContainsValue() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        assertFalse(map.containsValue("1"));
        map.put("a", "1");
        assertTrue(map.containsValue("1"));
        assertFalse(map.containsValue("2"));
    }

    @Test
    void testPutAll() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        final Map<String, String> otherMap = new HashMap<>();
        otherMap.put("x", "1");
        otherMap.put("y", "2");

        map.putAll(otherMap);
        assertEquals(2, map.size());
        assertEquals("1", map.get("x"));
        assertEquals("2", map.get("y"));
    }

    @Test
    void testClear() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        map.put("a", "1");
        map.put("b", "2");
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    void testGet() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        assertNull(map.get("a"));
        map.put("a", "1");
        assertEquals("1", map.get("a"));
    }

    @Test
    void testRemove() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), true);

        map.put("a", "1");
        assertEquals("1", map.remove("a"));
        assertNull(map.remove("a"));
    }

    @Test
    void testWeakKeyVariant() {
        final Map<String, String> map = new MildKeys<>(new LinkedHashMap<>(), false);

        map.put("a", "1");
        assertEquals("1", map.get("a"));
        assertEquals(1, map.entrySet().size());

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            assertNotNull(entry.getKey());
        }
    }
}
