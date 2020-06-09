# depict-maven-plugin

## requirements

- Java6 or higher
- Maven 2.0.11 or higher

## goals

### dependencies

Add the setting to your pom.xml.

```xml
            <plugin>
                <groupId>net.unit8.depict</groupId>
                <artifactId>depict-maven-plugin</artifactId>
                <version>0.1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>dependency</id>
                        <goals>
                            <goal>dependencies</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

When you execute `mvn package`, the plugin writes all dependencies to a `depice.dependencies.json` file.
