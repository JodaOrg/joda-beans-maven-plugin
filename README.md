
Joda-Beans plugin for Maven
---------------------------

This project provides a [Maven](https://maven.apache.org/) plugin
for [Joda-Beans](https://github.com/JodaOrg/joda-beans).


### Goals

This plugin has two goals - `validate` and `generate`.

#### Validate

The `validate` goal is used to check that no Joda-Beans need regeneration.
It can be used from the command line as `mvn joda-beans:validate`.

This goal has the following optional configuration items:
- `indent` - as per the command line, the amount of indentation used,
either the word "tab", or a number, such as "2" or "4". Default is "4". Property is `${joda.beans.indent}`.
- `prefix` - as per the command line, the prefix used by fields. Default is "". Property is `${joda.beans.prefix}`.
- `verbose` - as per the command line, a number from "0" (quiet) to "3" (verbose). Property is `${joda.beans.verbose}`.
- `stopOnError` - whether the build should continue when an error is found. Default is "true". Property is `${joda.beans.stopOnError}`.

The goal is bound to the `process-sources` phase of the lifecycle.


#### Generate

The `generate` goal is used to generate or regenerate any Joda-Beans in the project source directory.
It can be used from the command line as `mvn joda-beans:generate`.

This goal has the following optional configuration items:
- `indent` - as per the command line, the amount of indentation used,
either the word "tab", or a number, such as "2" or "4". Default is "4". Property is `${joda.beans.indent}`.
- `prefix` - as per the command line, the prefix used by fields. Default is "". Property is `${joda.beans.prefix}`.
- `verbose` - as per the command line, a number from "0" (quiet) to "3" (verbose). Property is `${joda.beans.verbose}`.

The goal is bound to the `process-sources` phase of the lifecycle.


### Setting up the pom

This section outlines the changes required in the pom for the plugin.


#### Declaration

The plugin will need to be declared in the pom to be used from the command line.
One way to achieve this is using `<pluginManagement>`:

```
  <build>
    <pluginManagement>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7.1</version>
      </plugin>
    </pluginManagement>
  </build>
```


#### Configuration

It is recommended that any configuration should be set using properties.
Note that in many cases, the default values of the properties (4 spaces and no prefix) will be fine.

If you do need to configure the plugin, your properties might look like this:

```
  <properties>
    <joda.beans.indent>2</joda.beans.indent>
    <joda.beans.prefix>_</joda.beans.prefix>
    ...
  </properties>
```

If you do not want to use properties, then a `<configuration>` block can be used instead.
However, in most cases, properties are easier to manage and clutter up your pom less.


#### Running as part of the main build

The following XML configuration will configure the plugin to run the validate goal during the
Maven lifecycle, ensuring that the Joda-Beans are up to date.
If they are not, the user would be expected to run `mvn joda-beans:generate` from the command line:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7.1</version>
        <executions>
          <execution>
            <id>joda-beans-validate</id>
            <goals>
              <goal>validate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

To generate the sources as part of the build, rather than validating them, use this configuration:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7.1</version>
        <executions>
          <execution>
            <id>joda-beans-generate</id>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

#### Joda-Beans version

Version 0.7.1 of the plugin hard codes a link to version 0.7 of Joda-Beans.
This can be overridden in the standard way using a `<dependencies>` block:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7.1</version>
        <executions>
          <execution>
            <id>joda-beans-generate</id>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.joda</groupId>
            <artifactId>joda-beans</artifactId>
            <version>0.8-FUTURE_VERSION</version>  <!-- Override Joda-Beans version -->
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
```
