# depict-gradle-plugin

[日本語](./README.ja.md)

## requirements

- Java6 or higher
- Gradle 3 or higher

## Tasks

### dependencies

Add the setting to your `build.gradle`.

```groovy
apply plugin: 'java'
apply plugin: 'net.unit8.depict'

task assembly {
    dependsOn('depictDependencies')
}

```

When you execute `mvn package`, the plugin writes all dependencies to a `depice.dependencies.json` file.
