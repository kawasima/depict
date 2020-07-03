# depict-maven-plugin

## requirements

- Java6 or higher
- Maven 2.0.11 or higher

## goals

### dependencies

依存関係をすべて出力します。

pom.xmlの project > build > plugins に以下の設定を書き加えてください

```xml
            <plugin>
                <groupId>net.unit8.depict</groupId>
                <artifactId>depict-maven-plugin</artifactId>
                <version>0.1.0</version>
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

デフォルトのゴールは package なので、 `mvn package` を実行すると、プロジェクト直下に `depict.dependencies.json` が出力されます。
