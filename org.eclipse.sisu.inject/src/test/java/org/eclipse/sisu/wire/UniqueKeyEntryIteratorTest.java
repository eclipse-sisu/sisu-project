/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.sisu.wire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class UniqueKeyEntryIteratorTest {

    @Test
    void testEmptyDelegate() {
        final UniqueKeyEntryIterator<String, String> it = new UniqueKeyEntryIterator<>(
                Collections.<Entry<String, String>>emptyList().iterator());

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testAllUniqueKeys() {
        final List<Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("a", 1));
        entries.add(new SimpleEntry<>("b", 2));
        entries.add(new SimpleEntry<>("c", 3));

        final UniqueKeyEntryIterator<String, Integer> it = new UniqueKeyEntryIterator<>(entries.iterator());

        assertTrue(it.hasNext());
        assertEquals("a", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("b", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("c", it.next().getKey());
        assertFalse(it.hasNext());
    }

    @Test
    void testDuplicateKeysAreSkipped() {
        final List<Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("a", 1));
        entries.add(new SimpleEntry<>("a", 2)); // duplicate – should be skipped
        entries.add(new SimpleEntry<>("b", 3));

        final UniqueKeyEntryIterator<String, Integer> it = new UniqueKeyEntryIterator<>(entries.iterator());

        assertTrue(it.hasNext());
        final Entry<String, Integer> first = it.next();
        assertEquals("a", first.getKey());
        assertEquals(1, first.getValue());

        assertTrue(it.hasNext());
        final Entry<String, Integer> second = it.next();
        assertEquals("b", second.getKey());
        assertEquals(3, second.getValue());

        assertFalse(it.hasNext());
    }

    @Test
    void testAllDuplicateKeys() {
        final List<Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("x", 1));
        entries.add(new SimpleEntry<>("x", 2));
        entries.add(new SimpleEntry<>("x", 3));

        final UniqueKeyEntryIterator<String, Integer> it = new UniqueKeyEntryIterator<>(entries.iterator());

        assertTrue(it.hasNext());
        final Entry<String, Integer> first = it.next();
        assertEquals("x", first.getKey());
        assertEquals(1, first.getValue());

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testMultipleDuplicatesInARow() {
        final List<Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("a", 1));
        entries.add(new SimpleEntry<>("b", 2));
        entries.add(new SimpleEntry<>("b", 3)); // duplicate
        entries.add(new SimpleEntry<>("b", 4)); // duplicate
        entries.add(new SimpleEntry<>("c", 5));

        final UniqueKeyEntryIterator<String, Integer> it = new UniqueKeyEntryIterator<>(entries.iterator());

        assertEquals("a", it.next().getKey());
        assertEquals("b", it.next().getKey());
        assertEquals("c", it.next().getKey());
        assertFalse(it.hasNext());
    }

    @Test
    void testHasNextIsIdempotent() {
        final List<Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("a", 1));

        final UniqueKeyEntryIterator<String, Integer> it = new UniqueKeyEntryIterator<>(entries.iterator());

        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());

        assertEquals("a", it.next().getKey());

        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    void testFirstEntryIsKeptForDuplicateKey() {
        final List<Entry<String, String>> entries = new ArrayList<>();
        entries.add(new SimpleEntry<>("key", "first"));
        entries.add(new SimpleEntry<>("key", "second"));

        final UniqueKeyEntryIterator<String, String> it = new UniqueKeyEntryIterator<>(entries.iterator());

        final Entry<String, String> entry = it.next();
        assertEquals("first", entry.getValue());
        assertFalse(it.hasNext());
    }
}
