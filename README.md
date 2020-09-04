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
- `config` - as per the command line, the config to use, "jdk" or "guava". Default is "guava". Property is `${joda.beans.config}`.
- `generatedAnnotation` - set to "true" to add `@Generated` annotations. Property is `${joda.beans.generatedAnnotation}`.
- `verbose` - as per the command line, a number from "0" (quiet) to "3" (verbose). Property is `${joda.beans.verbose}`.
- `stopOnError` - whether the build should continue when an error is found. Default is "true". Property is `${joda.beans.stopOnError}`.
- `skip` - skips the plugin

The goal is bound to the `process-sources` phase of the lifecycle by default.


#### Generate

The `generate` goal is used to generate or regenerate any Joda-Beans in the project source directory.
It can be used from the command line as `mvn joda-beans:generate`.

This goal has the following optional configuration items:
- `indent` - as per the command line, the amount of indentation used,
either the word "tab", or a number, such as "2" or "4". Default is "4". Property is `${joda.beans.indent}`.
- `prefix` - as per the command line, the prefix used by fields. Default is "". Property is `${joda.beans.prefix}`.
- `eol` - as per the command line, the line separator used to write generated files,
either "lf", "crlf", or "cr". Default is System.lineSeparator. Property is `${joda.beans.eol}`.
- `config` - as per the command line, the config to use, "jdk6" pr "guava". Default is "guava". Property is `${joda.beans.config}`.
- `generatedAnnotation` - set to "true" to add `@Generated` annotations. Property is `${joda.beans.generatedAnnotation}`.
- `verbose` - as per the command line, a number from "0" (quiet) to "3" (verbose). Property is `${joda.beans.verbose}`.
- `skip` - skips the plugin

The goal is bound to the `process-sources` phase of the lifecycle by default.


### Setting up the pom

This section outlines the changes required in the pom for the plugin.


#### Declaration

The plugin will need to be declared in the pom to be used from the command line.
One way to achieve this is using `<pluginManagement>`:

```
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.joda</groupId>
          <artifactId>joda-beans-maven-plugin</artifactId>
          <version>1.3.2</version>
        </plugin>
      </plugins>
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
        <artifactId>joda-beans-maven-plugin</artifactId>
        <version>1.3.2</version>
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
        <artifactId>joda-beans-maven-plugin</artifactId>
        <version>1.3.2</version>
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

#### Running with Eclipse and M2E

Joda-Beans can be used within Eclipse using Maven and M2E.
Version 1.0 and later of this plugin contains an additional XML configuration file for the M2E plugin.
This avoids the need for an additional M2E connector.

To get an even better integration, use Joda-Beans v1.5 or later and add the following to your project pom:

```
  <profiles>
    <profile>
      <id>joda-beans-eclipse</id>
      <activation>
        <property>
          <name>eclipse.buildId</name>
        </property>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>org.joda</groupId>
            <artifactId>joda-beans-maven-plugin</artifactId>
            <executions>
              <execution>
                <phase>generate-sources</phase>
                <goals>
                  <goal>generate-no-resolve</goal>
                </goals>
                <configuration>
                  <eclipse>true</eclipse>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
```

This profile only activates when running in Eclipse IDE.
It causes the `generate-no-resolve` goal to be executed using the special "eclipse=true" mode.
This mode means that when a bean is edited, the Joda-Beans source generator is triggered,
and the file recompiled. Note that the processing in Eclipse takes a few seconds to refresh properly.

(`generate-no-resolve` is a special mode from v1.2.0 that avoids resolving the classpath, which can give Eclipse problems)


#### Joda-Beans version

Version 0.7.3 and beyond of the plugin generates code using the Joda-Beans version in the project classpath.
If the project does not have the dependency available then generation is skipped.

Version 1.2.0 and beyond offers a goal `generate-no-resolve` that reads the property `joda.beans.version`
and resolves the dependency in the local repository. The version is defaulted to v2.5.0 of Joda-Beans.

Version 1.3.0 and beyond supports the property `joda.beans.generatedAnnotation`.
The Joda-Beans version is defaulted to v2.7.0 of Joda-Beans.

Version 1.3.2 and beyond supports the property `joda.beans.eol`.
The Joda-Beans version is defaulted to v2.8.1 of Joda-Beans.


### Release process

* Update version (README.md, not pom.xml)
* Commit and push
* `mvn clean release:clean release:prepare release:perform`
