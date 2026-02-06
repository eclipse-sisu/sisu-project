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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentMap;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class MildConcurrentValuesTest {
    @Test
    void testPutIfAbsent() {
        final ConcurrentMap<String, String> map = Soft.concurrentValues(16, 1);
        assertNull(map.putIfAbsent("a", "1"));
        assertEquals("1", map.get("a"));
        assertEquals("1", map.putIfAbsent("a", "2"));
        assertEquals("1", map.get("a"));
    }

    @Test
    void testReplace() {
        final ConcurrentMap<String, String> map = Soft.concurrentValues(16, 1);
        assertNull(map.replace("a", "1")); // no existing key
        map.put("a", "1");
        assertEquals("1", map.replace("a", "2"));
        assertEquals("2", map.get("a"));
    }

    @Test
    void testReplaceConditional() {
        final ConcurrentMap<String, String> map = Soft.concurrentValues(16, 1);
        map.put("a", "1");
        assertFalse(map.replace("a", "wrong", "2"));
        assertTrue(map.replace("a", "1", "2"));
        assertEquals("2", map.get("a"));
    }

    @Test
    void testRemoveConditional() {
        final ConcurrentMap<String, String> map = Soft.concurrentValues(16, 1);
        map.put("a", "1");
        assertFalse(map.remove("a", "wrong"));
        assertTrue(map.remove("a", "1"));
        assertNull(map.get("a"));
    }

    @Test
    void testBasicOperations() {
        final ConcurrentMap<String, String> map = Soft.concurrentValues(16, 1);
        assertTrue(map.isEmpty());
        map.put("x", "10");
        map.put("y", "20");
        assertEquals(2, map.size());
        map.remove("x");
        assertEquals(1, map.size());
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    void testWeakConcurrentValues() {
        final ConcurrentMap<String, String> map = Weak.concurrentValues(16, 1);
        assertNull(map.putIfAbsent("a", "1"));
        assertEquals("1", map.get("a"));
    }
}
