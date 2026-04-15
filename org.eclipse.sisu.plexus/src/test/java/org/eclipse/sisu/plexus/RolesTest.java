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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import java.util.List;
import java.util.Map;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.jupiter.api.Test;

public class RolesTest {
    private static final TypeLiteral<Object> OBJECT_LITERAL = TypeLiteral.get(Object.class);

    private static final TypeLiteral<String> STRING_LITERAL = TypeLiteral.get(String.class);

    private static final TypeLiteral<Integer> INTEGER_LITERAL = TypeLiteral.get(Integer.class);

    private static final Key<Object> OBJECT_COMPONENT_KEY = Key.get(Object.class);

    private static final Key<Object> OBJECT_FOO_COMPONENT_KEY = Key.get(Object.class, Names.named("foo"));

    @Test
    public void testCanonicalRoleHint() {
        assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), null));
        assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), ""));
        assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), "default"));
        assertEquals(OBJECT_LITERAL + ":foo", Roles.canonicalRoleHint(Object.class.getName(), "foo"));
        assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(component("")));
        assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(component("default")));
        assertEquals(OBJECT_LITERAL + ":foo", Roles.canonicalRoleHint(component("foo")));
    }

    @Test
    public void testDefaultComponentKeys() {
        assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(Object.class, null));
        assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(OBJECT_LITERAL, ""));
        assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(Object.class, "default"));
        assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(component("")));
        assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(component("default")));
    }

    @Test
    public void testComponentKeys() {
        assertEquals(OBJECT_FOO_COMPONENT_KEY, Roles.componentKey(Object.class, "foo"));
        assertEquals(OBJECT_FOO_COMPONENT_KEY, Roles.componentKey(component("foo")));
    }

    @Test
    public void testRoleAnalysis() {
        assertEquals(STRING_LITERAL, Roles.roleType(requirement(String.class), OBJECT_LITERAL));
        assertEquals(STRING_LITERAL, Roles.roleType(requirement(Object.class), STRING_LITERAL));

        assertEquals(
                STRING_LITERAL, Roles.roleType(requirement(Object.class), TypeLiteral.get(Types.listOf(String.class))));

        assertEquals(
                STRING_LITERAL, Roles.roleType(requirement(List.class), TypeLiteral.get(Types.listOf(String.class))));

        assertEquals(
                INTEGER_LITERAL,
                Roles.roleType(requirement(Object.class), TypeLiteral.get(Types.mapOf(String.class, Integer.class))));

        assertEquals(
                INTEGER_LITERAL,
                Roles.roleType(requirement(Map.class), TypeLiteral.get(Types.mapOf(String.class, Integer.class))));
    }

    private static Component component(final String hint) {
        return new ComponentImpl(Object.class, hint, Strategies.PER_LOOKUP, "");
    }

    @SuppressWarnings("deprecation")
    private static Requirement requirement(final Class<?> role) {
        return new RequirementImpl(role, false);
    }

    @Test
    public void testMissingComponentExceptions() {
        try {
            Roles.throwMissingComponentException(STRING_LITERAL, null);
            fail("Expected ProvisionException");
        } catch (final ProvisionException e) {
        }

        try {
            Roles.throwMissingComponentException(STRING_LITERAL, "foo");
            fail("Expected ProvisionException");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testCamelization() {
        assertSame("thisIsATest", Roles.camelizeName("thisIsATest"));
        assertEquals("thisIsATest", Roles.camelizeName("this-is-a-test"));
        assertEquals("TestingA", Roles.camelizeName("-testing-a"));
        assertEquals("testingB", Roles.camelizeName("testing-b-"));
        assertEquals("TestingC", Roles.camelizeName("--testing--c--"));
    }
}
