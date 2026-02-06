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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@BaseTests
class NamedIterableAdapterTest {
    @Test
    void testNamedAdapter() {
        final Map<Named, String> original = new LinkedHashMap<>();

        final Map<String, String> adapter = new EntryMapAdapter<>(new NamedIterableAdapter<>(original.entrySet()));

        assertEquals(original, adapter);
        original.put(Names.named("3"), "C");
        assertEquals(original, adapter);
        original.put(Names.named("1"), "A");
        assertEquals(original, adapter);
        original.put(Names.named("2"), "B");
        assertEquals(original, adapter);

        Assertions.assertEquals("{3=C, 1=A, 2=B}", adapter.toString());

        final Iterator<Entry<String, String>> i = adapter.entrySet().iterator();
        Assertions.assertEquals("3=C", i.next().toString());
        Assertions.assertEquals("1=A", i.next().toString());
        Assertions.assertEquals("2=B", i.next().toString());

        original.clear();

        assertEquals(original, adapter);
    }

    private static void assertEquals(final Map<Named, String> named, final Map<String, String> hinted) {
        final Iterator<Entry<Named, String>> i = named.entrySet().iterator();
        final Iterator<Entry<String, String>> j = hinted.entrySet().iterator();
        while (i.hasNext()) {
            assertTrue(j.hasNext());
            final Entry<Named, String> lhs = i.next();
            final Entry<String, String> rhs = j.next();
            Assertions.assertEquals(lhs.getKey().value(), rhs.getKey());
            Assertions.assertEquals(lhs.getValue(), rhs.getValue());
        }
        assertFalse(j.hasNext());
    }

    @Test
    void testUnsupportedOperations() {
        final Map<Named, String> original = new LinkedHashMap<>();

        final Map<String, String> adapter = new EntryMapAdapter<>(new NamedIterableAdapter<>(original.entrySet()));

        original.put(Names.named("1"), "A");

        try {
            adapter.entrySet().iterator().remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
        }

        try {
            adapter.entrySet().iterator().next().setValue("B");
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
        }
    }
}
