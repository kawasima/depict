# depict-maven-plugin

## requirements

- Java6 以上
- Maven 2.0.11 以上

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

#### depict.dependencies.jsonの仕様

npmの `package-lock.json` に似た形式で出力されます。

| Property | Description |
| :------ | :--- |
| name    | プロジェクトの識別子 (groupId:artifactId の形式) |
| version | プロジェクトのバージョン |
| dependencies | すべての依存ライブラリ |
| └ version  | 依存ライブラリのバージョン |
| └ type     | 依存ライブラリのartifactタイプ (jar等) |
| └ scope    | 依存ライブラリのスコープ (compile,runtime等) |
| └ optional | 依存関係がoptionalか否か |
| └ snapshot | スナップショットバージョンか否か |
| └ requires | 依存ライブラリが必要とする依存(pomに記載されたdependencies) |
| 　└ version | バージョン |
| 　└ type    | タイプ |
| 　└ scope   | スコープ |
| 　└ optional| optionalか否か |




