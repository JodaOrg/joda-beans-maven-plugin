
Joda-Beans plugin for Maven
---------------------------

This project provides a [Maven](https://maven.apache.org/) plugin
for [Joda-Beans](https://github.com/JodaOrg/joda-beans).


### Using the plugin

This plugin has two goals - `generate` and `validate`.

The `generate` goal is used to generate or regenerate any Joda-Beans in the project source directory.
It can be used from the command line as `mvn joda-beans:generate`.

The `validate` goal is used to check that no Joda-Beans need regeneration.
It can be used from the command line as `mvn joda-beans:validate`.

Both plugins have configuration items:
- `indent` - as per the command line, the amount of indentation used,
either the word "tab", or a number, such as "2" or "4". Default is "4".
- `prefix` - as per the command line, the prefix used by fields. Default is "".
- `verbose` - as per the command line, a number from "0" (quiet) to "3" (verbose)
- `stopOnError` - only for the `validate` goal, allowing builds to continue even when broken. Default is "true"

The first three can be referred to using properties -
`${joda.beans.indent}`, `${joda.beans.prefix}` and `${joda.beans.stopOnError}`


#### Using the plugin

The following XML configuration will configure the plugin to run during the first validation
phase of the Maven lifecycle, validating that the Joda-Beans are up to date.
If they are not, the user would be expected to run `mvn joda-beans:generate` from the command line:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7</version>
        <executions>
          <execution>
            <id>joda-beans-validate</id>
            <phase>validate</phase>
            <goals>
              <goal>validate</goal>
            </goals>
            <configuration>
              <indent>${joda.beans.indent}</indent>
              <prefix>${joda.beans.prefix}</prefix>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.joda</groupId>
            <artifactId>joda-beans</artifactId>
            <version>0.7</version>
          </dependency>
        </dependencies>
      </plugin>
      ...
    </plugins>
      ...
  </build>
  <properties>
    <joda.beans.indent>2</joda.beans.indent>
    <joda.beans.prefix>_</joda.beans.prefix>
    ...
  </properties>
```

To generate the sources as part of the build, rather than validating them, use this configuration:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.joda</groupId>
        <artifactId>maven-joda-beans-plugin</artifactId>
        <version>0.7</version>
        <executions>
          <execution>
            <id>joda-beans-generate</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>generate</goal>
            </goals>
            <configuration>
              <indent>${joda.beans.indent}</indent>
              <prefix>${joda.beans.prefix}</prefix>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.joda</groupId>
            <artifactId>joda-beans</artifactId>
            <version>0.7</version>
          </dependency>
        </dependencies>
      </plugin>
      ...
    </plugins>
      ...
  </build>
  <properties>
    <joda.beans.indent>2</joda.beans.indent>
    <joda.beans.prefix>_</joda.beans.prefix>
    ...
  </properties>
```

Note that the Joda-Beans dependency *must* be provided in the project's pom.xml.
This is a design choice, to ensure that the version of Joda-Beans is separate from the version of the plugin.

Note also the use of properties for the indent and prefix.
This is a design choice that allow usage from the command line without setting additional command line properties.

Note also that the indent and prefix properties only need to be set if different from the default values
of 4 and a blank string.

