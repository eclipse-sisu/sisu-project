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

import java.io.File;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.jupiter.api.Test;

class StringConverterTest {

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
    void explicitEmptyValueOverridesDefaultValue() throws ComponentConfigurationException {
        XmlPlexusConfiguration config = new XmlPlexusConfiguration("field");
        config.setValue("");
        config.setAttribute("default-value", "fallback");

        Object result =
                new StringConverter().fromConfiguration(null, config, String.class, null, null, ECHO_EVALUATOR, null);

        assertEquals("", result);
    }

    @Test
    void missingValueStillUsesDefaultValue() throws ComponentConfigurationException {
        XmlPlexusConfiguration config = new XmlPlexusConfiguration("field");
        config.setAttribute("default-value", "fallback");

        Object result =
                new StringConverter().fromConfiguration(null, config, String.class, null, null, ECHO_EVALUATOR, null);

        assertEquals("fallback", result);
    }
}
