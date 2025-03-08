# Named Index

Often Sisu's `SpaceModule` doesn't scan the full classpath at run time (for relevant annotations) but relies on a pre-generated index at `META-INF/sisu/javax.inject.Named`. This file contains the fully qualified class names (one class name per line). The line separator may be either `\n`, `\r` or `\r\n`.

## Generate the named index

There are two different ways how to create such an index file:

1. By leveraging the Java annotation processor in [`org.eclipse.sisu.space.SisuIndexAPT6`](https://github.com/eclipse-sisu/sisu-project/blob/main/org.eclipse.sisu.inject/src/main/java/org/eclipse/sisu/space/SisuIndexAPT6.java) to generate the index. One needs to enable it via [`javac -processor org.eclipse.sisu.space.SisuIndexAPT6`](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#annotation-processing) or with the according [`maven-compiler-plugin` parameter `annotationProcessors`](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#annotationProcessors).
2. By leveraging the dedicated [Maven plugin](../sisu-maven-plugin/index.html)
