<!-- MACRO{toc} -->

# Overview

This document provides some details on how to convert legacy Plexus components into modern JSR-330 components.

# Annotations

For brevity of examples imports are omitted. The following table defines the meanings and fully-qualified-class-names of the annotations references in the following examples.

| Annotation | Class | Description |
| --- | --- | --- |
| `@Component` | `org.codehaus.plexus.component.annotations.Component` | Legacy Plexus component annotation |
| `@Requirement` | `org.codehaus.plexus.component.annotations.Requirement` | Legacy Plexus injection annotation |
| `@Configuration` | `org.codehaus.plexus.component.annotations.Configuration` | Legacy Plexus configuration annotation |
| `@Named` | `javax.inject.Named` | Standard JSR-330 annotation to provide component name |
| `@Singleton` | `javax.inject.Singleton` | Standard JSR-330 annotation to mark component as singleton |
| `@Typed` | `javax.enterprise.inject.Typed` | JavaEE annotation to mark component type |
| `@Description` | `org.eclipse.sisu.Description` | Sisu-specific annotation to provide a description for a component |
| `@Parameters` | `org.eclipse.sisu.Parameters` | Sisu-specific annotation to mark \`Map\` injection as container context parameters. |
| `@Inject` | `javax.inject.Inject` | Standard JSR-330 annotation to mark field, parameter, method for injection |
| `@Nullable` | `javax.annotation.Nullable` | Standard JSR-305 annotation to mark field, parameter, result value as potentially returning null value |

**`javax.inject` vs. `com.google.inject`**

There are `com.google.inject` flavors of @Inject, @Named and @Singleton which should NOT be used.

Prefer the standard `javax.inject` versions.

## References

1. [JSR-330: Dependency Injection for Java](https://javax-inject.github.io/javax-inject/)
2. [JSR-305: Annotations for Software Defect Detection](https://www.jcp.org/en/jsr/detail?id=305)

# Code Examples

## @Component

Plexus @Component annotations are replaced by standard @Named, @Singleton, etc annotations.

### Singletons

For example this Plexus component:

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
}
```

can be converted to:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
}
```

**Naming Variants**

Components which are not directly looked up by names, or otherwise used in a context where the name is important you can omit the value for `@Named` and the full-qualified-class-name of the component will be used as the name instead.

```
package components;
@Named
@Singleton
public class MyComponent
    implements Component
{
}
```

Results in a component with implicit name of "components.MyComponent"

Additionally the Sisu Plexus integration has special handling for Plexus default components. Many Plexus applications define objects like:

```
@Component(role=Component.class)
public class DefaultComponent
    implements Component
{
}
```

The `Default` prefix on the component implementation class causes the binding to be translated to `@Named("default")`, so the conversion would look like:

```
package components;
@Named
@Singleton
public class DefaultComponent
    implements Component
{
}
```

BUT the name bound for this component would be "default".

### Instance

By default in Plexus, components are singletons, but this is not the case for every component. This Plexus component is not a singleton:

```
@Component(role=Component.class, hint="my", instantiationStrategy="per-lookup")
public class MyComponent
    implements Component
{
}
```

and is converted to:

```
@Named("my")
public class MyComponent
    implements Component
{
}
```

Notice this is the same as the example above, except with-out the @Singleton annotation.

Only per-lookup and singleton instantiation strategies have reasonable mappings into Sisu. The keep-alive and poolable strategies are not supported.

Additionally other Plexus-specific component configuration such as lifecycle-handlers, factories, composer, configurator, alias, version, profile, isolatedRealm are NOT supported.

### Type Override

By default the type of the component is determined automatically, though in some rare cases an explicit type is required. To specify the explicit binding type use the @Typed annotation:

```
@Named("my")
@Typed(Component.class)
public class MyComponent
    extends SomeSupportClassHardToGuessTypeFrom
{
}
```

### Descriptions

In some cases component descriptions are required. There is no standard annotation to provide this, however Sisu provides a custom annotation for this.

```
@Component(role=Component.class, hint="my", description="My custom component")
public class MyComponent
    implements Component
{
}
```

becomes:

```
@Named("my")
@Singleton
@Description("My custom component")
public class MyComponent
    implements Component
{
}
```

## @Requirement

### Basics

@Requirement defines injection points for legacy Plexus components. These more-or-less line-up directly with replacement with @Inject, though there are more options available as @Inject is support for fields, constructors and methods, where @Requirement only worked with fields. The recommended option is to replace legacy Plexus injection with constructor injection where possible.

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Requirement
    private AnotherComponent another;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final AnotherComponent another;

    @Inject
    public MyComponent(final AnotherComponent another) {
        this.another = another;
    }
}
```

Use of constructor injection in this fashion has some impact on replacing legacy Plexus lifecycle Initializable and Contextualizable interfaces, which often only exist to perform setup once injection is performed.

### Alternatives

Other options for conversions using field injection:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    @Inject
    private AnotherComponent another;
}
```

This is not recommended, as it makes it difficult to UNIT test the code w/o a full container to provide injection, which in itself can be problematic for UNIT testing. We highly recommend this form of injection NOT BE USED.

or method injection:

```
@Named("my")
@Singleton
public class MyComponent
implements Component
{
    private AnotherComponent another;

    @Inject
    public setAnotherComponent(final AnotherComponent another) {
        this.another = another;
    }
}
```

### Optional

Optional components are configured to be @Nullable

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Requirement(optional=true)
    private AnotherComponent another;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final AnotherComponent another;

    @Inject
    public MyComponent(final @Nullable AnotherComponent another) {
        this.another = another;
    }
}
```

### Names and Hints

Legacy Plexus component hints become @Named:

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Requirement(hint="foo")
    private AnotherComponent another;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final AnotherComponent another;

    @Inject
    public MyComponent(final @Named("foo") AnotherComponent another) {
        this.another = another;
    }
}
```

### Types

Legacy Plexus component roles, which are normally only used for collection types are generally not needed:

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Requirement(role=AnotherComponent.class)
    private List components;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
implements Component
{
    private final List components;

    @Inject
    public MyComponent(final List components) {
        this.components = components;
    }
}
```

## @Configuration

Plexus configuration injection is handled by \`@Inject @Named("${expression}")\` injection.

### Basics

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Configuration(name="configDir")
    private File configDir;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final File configDir;

    @Inject
    public MyComponent(final @Named("${configDir}") configDir) {
        this.configDir = configDir;
    }
}
```

### Defaults

Default values are provided by expression syntax.

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component
{
    @Configuration(name="configDir", value="defaultDir")
    private File configDir;
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final File configDir;

    @Inject
    public MyComponent(final @Named("${configDir:-defaultDir}") configDir) {
        this.configDir = configDir;
    }
}
```

## Lifecycle Support

This section is specific to how to adapt legacy Plexus component lifecycle interfaces. There is no hard-fast way to adapt these, but there are some guidelines to follow.

| Interface | Class | Description |
| --- | --- | --- |
| Initializable | org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable | Hook was used to inform a component once its injection has been performed. |
| Contextualizable | org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable | Similar to Initializable but passes in the container context. |
| Startable | org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable | Allows components to be started and stopped. |
| Disposable | org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable | Hook used to inform a component that it is no longer available in the container. |

### Initializable

By and far this can be replaced by using constructor-inject, and performing the initialize() at the end of the constructor.

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component, Initializable
{
    @Requirement
    private AnotherComponent another;

    public initialize() throws InitializationException {
        another.init();
    }
}
```

becomes:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final AnotherComponent another;

    @Inject
    public MyComponent(final AnotherComponent another) {
    this.another = another;
        another.init();
    }
}
```

### Contextualizable

Similar to Initializable, though if the context is needed you can inject the container context parameters with:

```
@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    @Inject
    public MyComponent(final @Parameters Map params) {
        // do something with \_context\_ params
    }
}
```

### Startable

Support for [Sisu/JSR250 lifecycle annotations](../org.eclipse.sisu.inject/lifecycle.html) is available when enabled for the Plexus wrapper ([Maven 3.5+](https://issues.apache.org/jira/browse/MNG-6084) enables JSR250 support). 

If JSR250 support is not enabled then applications can use other techniques to handle start/stop behavior. The examples below show the solution used in Sonatype Nexus, which relies on a modified implementation of the Google-Guava EventBus to manage lifecycle events.

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component, Startable
{
    public start() throws StartingException {
        // do something to "start"
    }

    public stop() throws StoppingException {
        // do something to "stop"
    }
}
```

becomes:

```
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final EventBus eventBus;

    @Inject
    public MyComponent(final EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public on(final NexusStartedEvent event) throws Exception {
        // do something to "start"
    }

    @Subscribe
    public on(final NexusStoppedEvent event) throws Exception {
        // do something to "stop"

        eventBus.unregister(this);
    }
}
```

### Disposable

Similar to Startable use of events are used to handle replacement for Disposable components.

```
@Component(role=Component.class, hint="my")
public class MyComponent
    implements Component, Disposable
{
    public dispose() {
        // do something to "dispose"
    }
}
```

becomes:

```
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Named("my")
@Singleton
public class MyComponent
    implements Component
{
    private final EventBus eventBus;

    @Inject
    public MyComponent(final EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public on(final NexusStoppedEvent event) throws Exception {
        // do something to "dispose"

        eventBus.unregister(this);
    }
}
```

## Custom Bindings

Plugins which require additional custom bindings can provide a @Named Guice module to configure components bindings further.

Sisu will automatically load modules which are @Named and apply them to the injectors bindings. These modules are really no different than normal Guice modules, except that they need to have the @Named annotation on them so that Sisu can locate them when initializing.

```
@Named
public class MyPluginModule
    extends com.google.inject.AbstractModule
{
    public void configure() {
        bind(Component.class).to(MyComponent.class);
    }
}
```

Note that Maven Class Loading mechanism does not expose the Guice API to plugins by default, you need to expose it yourself using the \`extension.xml\` config file. This file is where we list the exposed APIs from maven core to plugins.

Since core extensions are the only way to modify any behaviour of maven core, we have to create a core extension and put this file in it in order to modify the exposed APIs.

Even if you define custom Guice modules in your plugins by adding the Guice classes into your Classpath (by adding Guice libraries as dependencies in your pom.xml for example), they will not be picked up by maven due to the separation of Classpaths.

If you want to read more about maven class loader, see documentation [here](https://maven.apache.org/guides/mini/guide-maven-classloading.html#Core_Classloader).

To activate the Guice API, put the following file **extensions.xml** in the **/META-INF/maven/** folder of your core extension.

```
<extension>
    <exportedPackages>
        <exportedPackage>com.google.inject.*</exportedPackage>
        <exportedPackage>com.google.inject.binder.*</exportedPackage>
        <exportedPackage>com.google.inject.matcher.*</exportedPackage>
        <exportedPackage>com.google.inject.name.*</exportedPackage>
        <exportedPackage>com.google.inject.spi.*</exportedPackage>
        <exportedPackage>com.google.inject.util.*</exportedPackage>
        <exportedPackage>com.google.inject.internal.*</exportedPackage>
    </exportedPackages>
</extension>
```

Build your extensions as a jar, and put the its artifact identifiers in the **.mvn/extensions.xml** file to activate your extension, in the maven project where you want to use custom guice bindings.

```
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.1.0
                    https://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId/>
    <artifactId/>
    <version/>
  </extension>
</extensions>
```

See the documentation [here](https://maven.apache.org/ref/3.9.0/maven-core/core-extensions.html) for more details on the exposed APIs by default.

Here is the [documentation](https://maven.apache.org/guides/mini/guide-using-extensions.html) that explains how to define a core extension.
