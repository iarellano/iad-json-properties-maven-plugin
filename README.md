# iad-json-properties-maven-plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.iarellano/iad-json-properties-maven-plugin.svg)](https://mvnrepository.com/artifact/com.github.iarellano/iad-json-properties-maven-plugin) 


Support to import json files as project properties.

## About this plugin
This mojo can import json files as system properties using a pseudo-normalized json path as property names, it can also extract values using JsonPath and then assign the extracted values to project properties.

A sample project is provided in the [samples](./samples) directory. 
## Importing a json files
```xml
<plugin>
    <groupId>com.github.iarellano</groupId>
    <artifactId>iad-json-properties-maven-plugin</artifactId>
    <version>1.1</version>
    <executions>
        <execution>
            <id>parse-json-files</id>
            <phase>initialize</phase>
            <goals>
                <goal>load-json-properties</goal>
            </goals>
            <configuration>
                <skip>false</skip>
                <prefix>parentPrefix.</prefix>
                <files>
                    <file>
                        <prefix>testPrefix.</prefix>
                        <failIfFileNotFound>true</failIfFileNotFound>
                        <filePath>src/main/resources/test.json</filePath>
                    </file>
                    <file>
                        <charset>UTF-8</charset>
                        <prefix>arrayPrefix.</prefix>
                        <failIfFileNotFound>false</failIfFileNotFound>
                        <filePath>src/main/resources/array.json</filePath>
                    </file>
                </files>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Global parameters
Parameter | Description | Required
----------|-------------|---------
skip | Enable or disable plugin execution | No
prefix | Global prefix for the current execution configuration, if given then all imported properties will be prefixed by this value | No

#### File specific parameters
Parameter | Description | Required
----------|-------------|---------
filePath | Path to the json file, it can be absolute or relative to the project root directory | Yes 
prefix | Local prefix, if specified then all properties imported from the current file will have this prefrixed. | No
failIfFileNotFound | If true and filePath does not point to a valid json file then this mojo throws an exception. Default is **false** | No
charset | File charset. Default is **UTF-8** | No  


## Using JsonPath
This method makes use of [Jsonway Jsonpath](https://github.com/json-path/JsonPath) implementation to evaluate JsonPaths.
```xml
<plugin>
    <groupId>com.github.iarellano</groupId>
    <artifactId>iad-json-properties-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <id>extract-current-revision</id>
            <phase>initialize</phase>
            <goals>
                <goal>load-json-properties</goal>
            </goals>
            <configuration>
                <prefix>otherParentPrefix.</prefix>
                <skip>false</skip>
                <jsonPath>
                    <file>
                        <prefix>childPrefix.</prefix>
                        <filePath>src/main/resources/array.json</filePath>
                        <charset>UTF-8</charset>
                        <failIfFileNotFound>false</failIfFileNotFound>
                        <lookups>
                            <lookup>
                                <jsonPath>$.[1]</jsonPath>
                                <propertyName>item1.value</propertyName>
                                <failFast>true</failFast>
                            </lookup>
                        </lookups>
                    </file>
                    <file>
                        <prefix>suffix2.</prefix>
                        <filePath>src/main/resources/test.json</filePath>
                        <lookups>
                            <lookup>
                                <jsonPath>$.address.street</jsonPath>
                                <propertyName>address.street</propertyName>
                            </lookup>
                        </lookups>
                    </file>
                </jsonPath>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The only difference with with method 1 is that \<file> is enclosed in \<jsonPath> rather than in \<files> and now have lookup elements are defined within \<file>\<lookups>, therefore let's go straight to the definition of the \<lookup> parameters.
#### Lookup parameters
Parameter | Description | Required
----------|-------------|---------
jsonPath | JsonPath to evaluate | Yes
propertyName | Name of the property to which the result of the JsonPath evaluation wiil be assigned to | Yes
failFast | If true and the provided JsonPath does not evaluate to a primitive or null then an error is thrown. Default is **false** | No