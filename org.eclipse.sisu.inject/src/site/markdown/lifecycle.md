# Lifecycle Support

Each Sisu managed bean/component may optionally use annotations on methods to be called whenever the bean is started or stopped.

Those are

Annotation | Method called... | Leveraged Guice Hook
--- | ---
 `org.eclipse.sisu.PostConstruct` (or `javax.annotation.PostConstruct`) | Before bean is started | [`com.google.inject.spi.InjectionListener.afterInjection(...)`](https://google.github.io/guice/api-docs/6.0.0/javadoc/com/google/inject/spi/InjectionListener.html#afterInjection(I))
 `org.eclipse.sisu.PreDestroy` (or `javax.annotation.PreDestroy`) | Before bean is stopped | Is not called by default, only via [Plexus API](../org.eclipse.sisu.plexus/apidocs/org/codehaus/plexus/DefaultPlexusContainer.html).
 
 The annotations defined in [JSR 250](https://jcp.org/en/jsr/detail?id=250) are recognized in addition to the native Sisu ones when their classes are detected in the class path.
 
 The support needs to be explicitly enabled by passing an instance of [`org.eclipse.sisu.bean.LifecycleModule`](apidocs/org/eclipse/sisu/bean/LifecycleModule.html) when creating the `Injector`.