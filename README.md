Sisu is a modular [JSR330](https://javax-inject.github.io/javax-inject/)-based container that supports classpath scanning, auto-binding, and dynamic auto-wiring.

Sisu uses [Google-Guice](https://github.com/google/guice) to perform dependency injection and provide the core JSR330 support, but removes the need to write explicit bindings in Guice modules. Integration with other containers via the Eclipse Extension Registry and the OSGi Service Registry is a goal of this project.

## Maven

```xml
<dependency>
  <groupId>org.eclipse.sisu</groupId>
  <artifactId>org.eclipse.sisu.inject</artifactId>
  <version>0.3.5</version>
</dependency>
```

## How to Contribute

We accept contributions via GitHub pull requests. Please see [How To Contribute](CONTRIBUTING.md) to get started.

## License

- [Eclipse Public License, v1.0](http://www.eclipse.org/legal/epl-v10.html)

## Additional information

* Project Website: http://www.eclipse.org/sisu
* Mailing Lists: https://dev.eclipse.org/mailman/listinfo/sisu-dev
* Eclipse PMI: https://projects.eclipse.org/projects/technology.sisu

