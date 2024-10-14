<!-- MACRO{toc} -->

# Overview

The [Plexus Component Configurator API](apidocs/org/codehaus/plexus/component/configurator/package-summary.html) allows to configure components from a tree data structure ([`PlexusConfiguration`](apidocs/org/codehaus/plexus/configuration/PlexusConfiguration.html) objects) of `String` values. It is in most cases created from [XML](apidocs/org/codehaus/plexus/configuration/xml/XmlPlexusConfiguration.html). The most popular use case of this API is [Configuration of Maven Mojos](https://maven.apache.org/guides/mini/guide-configuring-plugins.html).

For each configuration element the class/type is being determined (from the object to be configured) and then the `String` value converted accordingly. For each class/type the rules for conversions are outlined below.

# Basic (Value) Objects

For conversion to primitive types their according [wrapper classes are used and automatically unboxed](https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html).

Class | Conversion from String | Since
---|---|---
`Boolean` | [`Boolean.valueOf(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Boolean.html#valueOf-java.lang.String-) | 
`Byte` | [`Byte.decode(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Byte.html#decode-java.lang.String-) |
`Character` | [`Character.valueOf(char)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#valueOf-char-) of the first character in the given string | 
`Class` | [`Class.forName(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#forName-java.lang.String-). *This conversion is not enabled by default*. |
`java.util.Date` | [`SimpleDateFormat.parse(String)`](https://docs.oracle.com/javase/8/docs/api/java/text/DateFormat.html#parse-java.lang.String-) for the following patterns: `yyyy-MM-dd hh:mm:ss.S a`, `yyyy-MM-dd hh:mm:ssa`, `yyyy-MM-dd HH:mm:ss.S` or `yyyy-MM-dd HH:mm:ss` | 
`Double` | [`Double.valueOf(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html#valueOf-java.lang.String-) | 
`Enum` | [`Enum.valueOf(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html#valueOf-java.lang.String-) | 
`java.io.File` | [`new File(String)`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html#File-java.lang.String-) with the file separators normalized to `File.separatorChar`. In case the file is relative, it is made absolute by calling the given [`ExpressionEvaluator's alignToBaseDirectory(File)` method](apidocs/org/codehaus/plexus/component/configurator/expression/ExpressionEvaluator.html#alignToBaseDirectory(java.io.File)). | 
`Float` | [`Float.valueOf(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Float.html#valueOf-java.lang.String-) | 
`Integer` | [`Integer.decode(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html#decode-java.lang.String-) | 
`Long` | [`Long.decode(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Long.html#decode-java.lang.String-) | 
`Short` | [`Short.decode(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/Short.html#decode-java.lang.String-) | 
`String` | n/a | 
`StringBuffer` | [`new StringBuffer(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuffer.html#StringBuffer-java.lang.String-) | 
`StringBuilder` | [`new StringBuilder(String)`](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html#StringBuilder-java.lang.String-) | 
`java.net.URI` | [`new URI(String)`](https://docs.oracle.com/javase/8/docs/api/java/net/URI.html#URI-java.lang.String-) | 
`java.net.URL` | [`new URL(String)`](https://docs.oracle.com/javase/8/docs/api/java/net/URL.html#URL-java.lang.String-) |
`org.codehaus.plexus.configuration.PlexusConfiguration` | the given `PlexusConfiguration` is passed as is (no conversion) | 
`java.nio.file.Path` | same as `java.io.File` converted via [`toPath()`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html#toPath--) | 0.9.0.M3
`java.time.temporal.Temporal` | created from via [`DateTimeFormatter.parse(String)`](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#parse-java.lang.CharSequence-) supporting same patterns as `java.util.Date`. Supports the following [`Temporal`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/Temporal.html) classes: [`LocalDate`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/LocalDate.html), [`LocalDateTime`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/LocalDateTime.html), [`LocalTime`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/LocalTime.html), [`Instant`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/Instant.html), [`OffsetDateTime`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/OffsetDateTime.html), [`OffsetTime`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/OffsetTime.html) and [`ZonedDateTime`](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/ZonedDateTime.html) | 0.9.0.M3
`*` | only used as last resort, try class's constructor taking a single `String` value | 0.9.0.M3

# Composite Objects

## Collection Objects

The **collection's implementation class** is in most cases determined automatically according to the following table

Default Collection Class | Used for
--- | ---
`TreeSet` | all types assignable to [`SortedSet`](https://docs.oracle.com/javase/8/docs/api/java/util/SortedSet.html)
`HashSet` | all types assignable to [`Set`](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html)
`ArrayList` | every other [`Collection`](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html) type which is not a [`Map`](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html)
`Properties` | all types assignable to [`Properties`](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html)
`TreeMap` | all types assignable to [`Map`](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html) but not [`Properties`](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html)
`?[]` | all array types

In order to use a different class, one can leverage the attribute `implementation` which is supposed to contain the fully qualified class name.

The **item type** of the collection is in most cases determined automatically (from the parameterized type) but it can be overwritten:

1. If the first item element contains an `implementation` attribute, try to load the class with the given fully qualified class name from the attribute value
1. If the first item element name contains a `.`, try to load the class with the fully qualified class name given in the element name
1. Try the first item element name (with capitalized first letter) as a class in the same package as the mojo/object being configured

The value is either given in child leaf element values (the name does not matter) or for arrays and non-`Map` collection types as String value in the collection element itself (where comma is used as separator between the individual item values).
For `Map`s the the configuration element's name is used as key, while the configuration element's value is used as value.

## Complex Objects

All other classes try to inject each leaf element individually. For that the following lookup order is used

1. method `set<name>`
1. method `add<name>`
1. field `name`

The first found method/field is used to set the value via reflection (via method call or field setter).
