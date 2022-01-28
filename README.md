```
+-----------+---+-----------+---+---+
|           |   |           |   |   |
|   O-------+   |   O-------+   |   |
|           |   |           |   |   |
+-------O   |   +-------O   |   O   |
|           |   |           |       |
+-----------+---+-----------+-------+
```

[![CI](https://github.com/eclipse/sisu.inject/actions/workflows/ci.yml/badge.svg?event=push)](https://github.com/eclipse/sisu.inject/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-EPL_1.0-blue.svg)](https://www.eclipse.org/legal/epl-v10.html)

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

## Documentation

[Sisu in 5 minutes](https://eclipse.github.io/sisu.inject/)

[Javadoc](https://eclipse.github.io/sisu.inject/apidocs/)

## How to Contribute

We accept contributions via GitHub pull requests. Please see [How To Contribute](CONTRIBUTING.md) to get started.

## License

- [Eclipse Public License, v1.0](https://www.eclipse.org/legal/epl-v10.html)

## Meaning of sisu
<sub>From https://en.wikipedia.org/w/index.php?title=Sisu&oldid=371994592</sub>

> Sisu is a Finnish term loosely translated into English as strength of will, determination, perseverance, and acting rationally in the face of adversity. However, the word is widely considered to lack a proper translation into any language. Sisu has been described as being integral to understanding Finnish culture. The literal meaning is equivalent in English to "having guts", and the word derives from sisus, which means something inner or interior. However sisu is defined by a long-term element in it; it is not momentary courage, but the ability to sustain an action against the odds. Deciding on a course of action and the sticking to that decision against repeated failures is sisu. It is similar to equanimity, except the forbearance of sisu has a grimmer quality of stress management than the latter. The noun sisu is related to the adjective sisukas, one having the quality of sisu.

## Additional information

* Project Website: https://www.eclipse.org/sisu
* Mailing Lists: https://dev.eclipse.org/mailman/listinfo/sisu-dev
* Eclipse PMI: https://projects.eclipse.org/projects/technology.sisu

