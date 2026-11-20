/*
 * Copyright (c) 2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sisu.plexus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InterpolationsTest {

    @Test
    void substitutesKnownVariables() {
        final Map<String, Object> variables = new HashMap<>();
        variables.put("group", "org.example");
        variables.put("artifact", "demo");

        assertEquals("org.example:demo:1.0", Interpolations.interpolate("${group}:${artifact}:1.0", variables));
    }

    @Test
    void leavesUnknownVariablesUntouched() {
        final Map<String, Object> variables = new HashMap<>();
        variables.put("known", "value");

        assertEquals("${unknown}/value", Interpolations.interpolate("${unknown}/${known}", variables));
    }

    @Test
    void removesVariablesWithEmptyValues() {
        final Map<String, Object> variables = new HashMap<>();
        variables.put("empty", "");

        assertEquals("a//b", Interpolations.interpolate("a/${empty}/b", variables));
    }

    @Test
    void handlesTextWithoutTokensOrVariables() {
        assertEquals("plain text", Interpolations.interpolate("plain text", new HashMap<>()));
        assertEquals("plain text", Interpolations.interpolate("plain text", null));
        assertEquals(null, Interpolations.interpolate(null, new HashMap<>()));
    }

    @Test
    void interpolatesReaderContents() throws Exception {
        final Map<String, Object> variables = new HashMap<>();
        variables.put("key", "value");

        try (Reader reader = Interpolations.interpolatingReader(new StringReader("<${key}>"), variables)) {
            assertEquals("<value>", Interpolations.toString(reader));
        }
    }
}
