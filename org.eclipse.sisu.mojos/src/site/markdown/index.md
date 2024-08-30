# Introduction

 The Sisu Plugin for Maven provides mojos to generate `META-INF/sisu/javax.inject.Named` index files for the [Sisu container](../org.eclipse.sisu.inject/index.html).

# Usage

## Indexing individual projects

```
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.sisu</groupId>
        <artifactId>sisu-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>index-project</id>
            <goals>
              <goal>main-index</goal>
              <goal>test-index</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

## Indexing assembled applications

```
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.sisu</groupId>
        <artifactId>sisu-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>index-dependencies</id>
            <phase>package</phase>
            <goals>
              <goal>index</goal>
            </goals>
            <configuration>
              <!-- same include/exclude settings as maven-dependency-plugin -->
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```
