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
package org.eclipse.sisu.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.inject.ImplementedBy;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.eclipse.sisu.BaseTests;
import org.junit.jupiter.api.Test;

@BaseTests
class TypeArgumentsTest {
    static TypeLiteral<Object> OBJECT_TYPE = TypeLiteral.get(Object.class);

    static TypeLiteral<String> STRING_TYPE = TypeLiteral.get(String.class);

    static TypeLiteral<Float> FLOAT_TYPE = TypeLiteral.get(Float.class);

    static TypeLiteral<Short> SHORT_TYPE = TypeLiteral.get(Short.class);

    static TypeLiteral<Number> NUMBER_TYPE = TypeLiteral.get(Number.class);

    @SuppressWarnings("rawtypes")
    List rawList;

    List<Short> shortList;

    List<?> wildcardList;

    List<? extends String> wildcardStringList;

    @SuppressWarnings("rawtypes")
    Map rawMap;

    Map<String, Float> stringFloatMap;

    Map<?, ?> wildcardMap;

    Map<? extends Float, ? extends Short> wildcardFloatShortMap;

    interface CallableNumber<T extends Number> extends Callable<T> {}

    @Test
    void testTypeArguments() {
        TypeLiteral<?>[] types;

        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("rawList"), 0));
        types = TypeArguments.get(getFieldType("rawList"));
        assertEquals(0, types.length);

        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("rawMap"), 0));
        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("rawMap"), 1));
        types = TypeArguments.get(getFieldType("rawMap"));
        assertEquals(0, types.length);

        assertEquals(SHORT_TYPE, TypeArguments.get(getFieldType("shortList"), 0));
        types = TypeArguments.get(getFieldType("shortList"));
        assertEquals(1, types.length);
        assertEquals(SHORT_TYPE, types[0]);

        assertEquals(STRING_TYPE, TypeArguments.get(getFieldType("stringFloatMap"), 0));
        assertEquals(FLOAT_TYPE, TypeArguments.get(getFieldType("stringFloatMap"), 1));
        types = TypeArguments.get(getFieldType("stringFloatMap"));
        assertEquals(2, types.length);
        assertEquals(STRING_TYPE, types[0]);
        assertEquals(FLOAT_TYPE, types[1]);

        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("wildcardList"), 0));
        types = TypeArguments.get(getFieldType("wildcardList"));
        assertEquals(1, types.length);
        assertEquals(OBJECT_TYPE, types[0]);

        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("wildcardMap"), 0));
        assertEquals(OBJECT_TYPE, TypeArguments.get(getFieldType("wildcardMap"), 1));
        types = TypeArguments.get(getFieldType("wildcardMap"));
        assertEquals(2, types.length);
        assertEquals(OBJECT_TYPE, types[0]);
        assertEquals(OBJECT_TYPE, types[1]);

        assertEquals(STRING_TYPE, TypeArguments.get(getFieldType("wildcardStringList"), 0));
        types = TypeArguments.get(getFieldType("wildcardStringList"));
        assertEquals(1, types.length);
        assertEquals(STRING_TYPE, types[0]);

        assertEquals(FLOAT_TYPE, TypeArguments.get(getFieldType("wildcardFloatShortMap"), 0));
        assertEquals(SHORT_TYPE, TypeArguments.get(getFieldType("wildcardFloatShortMap"), 1));
        types = TypeArguments.get(getFieldType("wildcardFloatShortMap"));
        assertEquals(2, types.length);
        assertEquals(FLOAT_TYPE, types[0]);
        assertEquals(SHORT_TYPE, types[1]);

        final TypeLiteral<?> genericSuperType =
                TypeLiteral.get(CallableNumber.class).getSupertype(Callable.class);

        assertEquals(NUMBER_TYPE, TypeArguments.get(genericSuperType, 0));
        types = TypeArguments.get(genericSuperType);
        assertEquals(1, types.length);
        assertEquals(NUMBER_TYPE, types[0]);
    }

    @SuppressWarnings("rawtypes")
    List[] rawListArray;

    List<Short>[] shortListArray;

    List<?>[] wildcardListArray;

    List<? extends String>[] wildcardStringListArray;

    @SuppressWarnings("rawtypes")
    Map[] rawMapArray;

    Map<String, Float>[] stringFloatMapArray;

    Map<?, ?>[] wildcardMapArray;

    Map<? extends Float, ? extends Short>[] wildcardFloatShortMapArray;

    List<String[]> stringArrayList;

    @Test
    void testComponentType() {
        TypeLiteral<?>[] types;

        types = TypeArguments.get(getFieldType("rawListArray"));
        assertEquals(getFieldType("rawList"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("rawListArray"), 0));
        assertEquals(List.class, types[0].getType());

        types = TypeArguments.get(getFieldType("rawMapArray"));
        assertEquals(getFieldType("rawMap"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("rawMapArray"), 0));
        assertEquals(Map.class, types[0].getType());

        types = TypeArguments.get(getFieldType("shortListArray"));
        assertEquals(getFieldType("shortList"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("shortListArray"), 0));
        assertEquals(Types.listOf(Short.class), types[0].getType());

        types = TypeArguments.get(getFieldType("stringFloatMapArray"));
        assertEquals(getFieldType("stringFloatMap"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("stringFloatMapArray"), 0));
        assertEquals(Types.mapOf(String.class, Float.class), types[0].getType());

        types = TypeArguments.get(getFieldType("wildcardListArray"));
        assertEquals(getFieldType("wildcardList"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("wildcardListArray"), 0));
        assertEquals(Types.listOf(Types.subtypeOf(Object.class)), types[0].getType());

        types = TypeArguments.get(getFieldType("wildcardMapArray"));
        assertEquals(getFieldType("wildcardMap"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("wildcardMapArray"), 0));
        assertEquals(Types.mapOf(Types.subtypeOf(Object.class), Types.subtypeOf(Object.class)), types[0].getType());

        types = TypeArguments.get(getFieldType("wildcardStringListArray"));
        assertEquals(getFieldType("wildcardStringList"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("wildcardStringListArray"), 0));
        assertEquals(Types.listOf(Types.subtypeOf(String.class)), types[0].getType());

        types = TypeArguments.get(getFieldType("wildcardFloatShortMapArray"));
        assertEquals(getFieldType("wildcardFloatShortMap"), types[0]);
        assertEquals(types[0], TypeArguments.get(getFieldType("wildcardFloatShortMapArray"), 0));
        assertEquals(Types.mapOf(Types.subtypeOf(Float.class), Types.subtypeOf(Short.class)), types[0].getType());

        types = TypeArguments.get(TypeArguments.get(getFieldType("stringArrayList"))[0]);
        assertEquals(STRING_TYPE, types[0]);
        assertEquals(types[0], TypeArguments.get(TypeArguments.get(getFieldType("stringArrayList"), 0), 0));
    }

    @Test
    void testTypeArgumentRangeChecks() {
        try {
            TypeArguments.get(getFieldType("stringFloatMap"), -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }
        try {
            TypeArguments.get(getFieldType("stringFloatMap"), 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }

        try {
            TypeArguments.get(getFieldType("wildcardStringListArray"), -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }
        try {
            TypeArguments.get(getFieldType("wildcardStringListArray"), 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }
    }

    static class CallableImpl<T> implements Callable<T> {
        @Override
        public T call() throws Exception {
            return null;
        }
    }

    static class CallableNumberImpl<T extends Number> implements CallableNumber<T> {
        @Override
        public T call() throws Exception {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    static class CallableListImpl implements Callable<List> {
        @Override
        public List call() throws Exception {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testIsAssignableFrom() {
        // === simple types ===

        assertTrue(TypeArguments.isAssignableFrom(TypeLiteral.get(Object.class), TypeLiteral.get(String.class)));
        assertTrue(TypeArguments.isAssignableFrom(TypeLiteral.get(Number.class), TypeLiteral.get(Short.class)));
        assertTrue(TypeArguments.isAssignableFrom(TypeLiteral.get(Collection.class), TypeLiteral.get(Set.class)));

        // === generic types ===

        assertFalse(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<Collection>>() {},
                TypeLiteral.get(CallableListImpl.class))); // not assignable since no wild-card
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<List>>() {}, TypeLiteral.get(CallableListImpl.class)));
        assertFalse(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<String>>() {}, TypeLiteral.get(CallableListImpl.class)));

        // === unbound type-variables ===

        assertTrue(TypeArguments.isAssignableFrom(new TypeLiteral<Callable>() {}, TypeLiteral.get(Callable.class)));
        assertTrue(TypeArguments.isAssignableFrom(new TypeLiteral<Callable>() {}, TypeLiteral.get(CallableImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<String>>() {}, TypeLiteral.get(CallableImpl.class)));
        assertFalse(
                TypeArguments.isAssignableFrom(new TypeLiteral<CallableImpl>() {}, TypeLiteral.get(Callable.class)));

        // === bound type-variables ===

        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<CallableNumber>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<CallableNumber<Number>>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<CallableNumber<Float>>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertFalse(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<String>>() {},
                TypeLiteral.get(CallableNumberImpl.class))); // mismatched type-bounds

        // === unbound wild-cards ===

        assertTrue(
                TypeArguments.isAssignableFrom(new TypeLiteral<Callable<?>>() {}, TypeLiteral.get(CallableImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<?>>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<CallableNumber<?>>() {}, TypeLiteral.get(CallableNumberImpl.class)));

        // === bound wild-cards ===

        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<? extends Collection>>() {}, TypeLiteral.get(CallableListImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<? extends Number>>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertTrue(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<? extends Float>>() {}, TypeLiteral.get(CallableNumberImpl.class)));
        assertFalse(TypeArguments.isAssignableFrom(
                new TypeLiteral<Callable<? extends String>>() {}, TypeLiteral.get(CallableNumberImpl.class)));

        // === array types ===

        assertTrue(TypeArguments.isAssignableFrom(
                TypeLiteral.get(Types.arrayOf(Object.class)), TypeLiteral.get(Types.arrayOf(String.class))));
        assertTrue(TypeArguments.isAssignableFrom(
                TypeLiteral.get(Types.arrayOf(Number.class)), TypeLiteral.get(Types.arrayOf(Float.class))));

        // === mismatched types ===

        assertFalse(TypeArguments.isAssignableFrom(
                TypeLiteral.get(Types.arrayOf(Object.class)), TypeLiteral.get(Types.listOf(Object.class))));
        assertFalse(TypeArguments.isAssignableFrom(
                TypeLiteral.get(Types.listOf(Object.class)), TypeLiteral.get(Types.arrayOf(Object.class))));

        // === corner case ===

        final Type T = (Type) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {TypeVariable.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args)
                            throws Throwable {
                        final String name = method.getName();
                        if ("getBounds".equals(name)) {
                            return new Type[] {String.class};
                        }
                        if ("getName".equals(name)) {
                            return "T";
                        }
                        if ("hashCode".equals(name)) {
                            return hashCode();
                        }
                        if ("equals".equals(name)) {
                            return equals(args[0]);
                        }
                        return null;
                    }
                });

        final Type callableT = (Type) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {ParameterizedType.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args)
                            throws Throwable {
                        final String name = method.getName();
                        if ("getActualTypeArguments".equals(name)) {
                            return new Type[] {T};
                        }
                        if ("getRawType".equals(name)) {
                            return Callable.class;
                        }
                        if ("hashCode".equals(name)) {
                            return hashCode();
                        }
                        if ("equals".equals(name)) {
                            return equals(args[0]);
                        }
                        return null;
                    }
                });

        assertFalse(TypeArguments.isAssignableFrom(TypeLiteral.get(callableT), TypeLiteral.get(Callable.class)));

        assertFalse(
                TypeArguments.isAssignableFrom(TypeLiteral.get(callableT), TypeLiteral.get(CallableNumberImpl.class)));
    }

    @Test
    void testIsConcrete() {
        assertFalse(TypeArguments.isConcrete(Map.class));
        assertFalse(TypeArguments.isConcrete(AbstractMap.class));
        assertTrue(TypeArguments.isConcrete(HashMap.class));

        assertFalse(TypeArguments.isConcrete(new TypeLiteral<Map<String, String>>() {}));
        assertFalse(TypeArguments.isConcrete(new TypeLiteral<AbstractMap<String, String>>() {}));
        assertTrue(TypeArguments.isConcrete(new TypeLiteral<HashMap<String, String>>() {}));
    }

    @ImplementedBy(Object.class)
    static interface Implicit1<T> {}

    static class SomeProvider implements Provider<Object> {
        @Override
        public Object get() {
            return null;
        }
    }

    @ProvidedBy(SomeProvider.class)
    static interface Implicit2<T> {}

    @Test
    void testIsImplicit() {
        assertFalse(TypeArguments.isImplicit(Map.class));
        assertFalse(TypeArguments.isImplicit(AbstractMap.class));
        assertTrue(TypeArguments.isImplicit(HashMap.class));

        assertFalse(TypeArguments.isImplicit(new TypeLiteral<Map<String, String>>() {}));
        assertFalse(TypeArguments.isImplicit(new TypeLiteral<AbstractMap<String, String>>() {}));
        assertTrue(TypeArguments.isImplicit(new TypeLiteral<HashMap<String, String>>() {}));

        assertTrue(TypeArguments.isImplicit(Implicit1.class));
        assertTrue(TypeArguments.isImplicit(Implicit2.class));

        assertTrue(TypeArguments.isImplicit(new TypeLiteral<Implicit1<String>>() {}));
        assertTrue(TypeArguments.isImplicit(new TypeLiteral<Implicit2<String>>() {}));
    }

    private static TypeLiteral<?> getFieldType(final String name) {
        try {
            return TypeLiteral.get(
                    TypeArgumentsTest.class.getDeclaredField(name).getGenericType());
        } catch (final NoSuchFieldException e) {
            throw new IllegalArgumentException("Unknown test field " + name);
        }
    }
}
