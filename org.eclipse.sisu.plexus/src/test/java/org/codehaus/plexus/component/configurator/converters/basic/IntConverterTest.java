/*
 * Copyright (c) 2010-2026 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.codehaus.plexus.component.configurator.converters.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.jupiter.api.Test;

class IntConverterTest {

    // echoes back any value that is not an expression, same as Maven's own PluginParameterExpressionEvaluator
    private static final ExpressionEvaluator ECHO_EVALUATOR = new ExpressionEvaluator() {
        @Override
        public Object evaluate(String expression) throws ExpressionEvaluationException {
            return expression;
        }

        @Override
        public File alignToBaseDirectory(File path) {
            return null;
        }
    };

    @Test
    void explicitEmptyValueStillFallsBackToDefaultValue() throws ComponentConfigurationException {
        // unlike String, an empty value is not meaningful for an int: "" cannot be parsed as a number,
        // so it must still fall back to default-value exactly as an absent element would.
        XmlPlexusConfiguration config = new XmlPlexusConfiguration("field");
        config.setValue("");
        config.setAttribute("default-value", "42");

        Object result =
                new IntConverter().fromConfiguration(null, config, Integer.class, null, null, ECHO_EVALUATOR, null);

        assertEquals(42, result);
    }

    @Test
    void explicitEmptyValueWithoutDefaultValueLeavesFieldUntouched() throws ComponentConfigurationException {
        XmlPlexusConfiguration config = new XmlPlexusConfiguration("field");
        config.setValue("");

        Object result =
                new IntConverter().fromConfiguration(null, config, Integer.class, null, null, ECHO_EVALUATOR, null);

        assertNull(result);
    }
}
