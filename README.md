```
+-----------+---+-----------+---+---+
|           |   |           |   |   |
|   O-------+   |   O-------+   |   |
|           |   |           |   |   |
+-------O   |   +-------O   |   O   |
|           |   |           |       |
+-----------+---+-----------+-------+
```

[![build](https://github.com/eclipse/sisu.inject/actions/workflows/build.yml/badge.svg?event=push)](https://github.com/eclipse/sisu.inject/actions/workflows/build.yml)
[![maintainability](https://sonarcloud.io/api/project_badges/measure?project=eclipse-sisu_sisu.inject&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=eclipse-sisu_sisu.inject)
[![license](https://img.shields.io/badge/license-EPL_2.0-blue.svg)](https://www.eclipse.org/legal/epl/epl-v20.html)

Sisu is a modular [JSR-330](https://javax-inject.github.io/javax-inject/)-based container that supports classpath scanning, auto-binding, and dynamic auto-wiring.

Sisu uses [Google Guice](https://github.com/google/guice) to perform dependency injection and provide the core JSR-330 support, but removes the need to write explicit bindings in Guice modules. Integration with other containers via the Eclipse Extension Registry and the OSGi Service Registry is a goal of this project.

## Documentation

* [Sisu Website](https://eclipse.dev/sisu/)
* [Eclipse Project Information](https://projects.eclipse.org/projects/technology.sisu)

## How to Contribute

We accept contributions via GitHub pull requests. Please see [How To Contribute](CONTRIBUTING.md) to get started.

## License

- [Eclipse Public License, v2.0](https://www.eclipse.org/legal/epl-v20.html)

## Requirements

Runtime requirements:
* Java 8

Build time requirements:
* Java 17+
* Maven 3.9.12+
