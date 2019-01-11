# Example usage of iad-json-properties-maven-plugin
Support to import json files as project properties.

The folling command will import the contents of [src/main/resources/array.json](./src/main/resources/array.json) and [src/main/resources/test.json](./src/main/resources/test.json) as system properties and then will print them as part of the build.
```bash
mvn clean compile
```