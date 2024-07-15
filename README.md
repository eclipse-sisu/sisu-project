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
[![license](https://img.shields.io/badge/license-EPL_1.0-blue.svg)](https://www.eclipse.org/legal/epl-v10.html)

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

[Sisu in 5 minutes](https://eclipse-sisu.github.io/sisu-project/index.html)

[Javadoc](https://eclipse-sisu.github.io/sisu-project/javadoc.html)

[Plexus to JSR330](https://eclipse-sisu.github.io/sisu-project/plexus/index.html)

### Generation of Named Index

Often Sisu's `SpaceModule` doesn't scan the full classpath at run time (for relevant annotations) but relies on a pre-generated index at `META-INF/sisu/javax.inject.Named`. There are two different ways how to create such an index file:

1. By leveraging the Java annotation processor in [org.eclipse.sisu.space.SisuIndexAPT6](https://github.com/eclipse-sisu/sisu-project/blob/main/org.eclipse.sisu.inject/src/main/java/org/eclipse/sisu/space/SisuIndexAPT6.java) to generate the index. One needs to enable via [`javac -processor org.eclipse.sisu.space.SisuIndexAPT6`](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#annotation-processing) or with the according [`maven-compiler-plugin` parameter `annotationProcessors`](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#annotationProcessors).
2. By leveraging the dedicated Maven plugin <https://github.com/eclipse-sisu/sisu-project/tree/main/org.eclipse.sisu.mojos>

## Related projects

https://github.com/eclipse/sisu.mojos/
 * a [Maven](https://maven.apache.org/) plugin that generates annotation indexes for Sisu to avoid classpath scanning at runtime

https://github.com/eclipse/sisu.plexus/
 * an extension that implements [Plexus](https://codehaus-plexus.github.io/#Plexus_History) container API and injection semantics on top of Sisu

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

