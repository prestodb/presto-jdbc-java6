# presto-jdbc-java6

[![travis](https://api.travis-ci.org/prestodb/presto-jdbc-java6.svg)](https://travis-ci.org/prestodb/presto-jdbc-java6)

Presto JDBC driver compatible with Java 6

# Motivation 

Many companies are still using Java 6 VM's for for old systems. 
The purpose of this project to allow such companies to connect
to Presto database through JDBC. This cannot be done using official
Presto JDBC driver as it requires Java 8 VM.

# Building

## Setup JDK 6 toolchain

Build script is `mvn` based and depends on [`toolchains`](https://maven.apache.org/guides/mini/guide-using-toolchains.html) feature of Maven.

We need that as new version of maven require JRE 7 to run itself.
And we want to compile project with JDK 6.

To configure Java 6 toolchain in `~/.m2/toolchains.xml` put

```
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
    <!-- JDK toolchains -->
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>1.6</version>
            <vendor>sun</vendor>
        </provides>
        <configuration>
          <jdkHome>/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

Replace contents of `jdkHome` with path matching your machine.

## Build

To build without tests execute:

```
mvn install -DskipTests=true  
```

## Tests

To run integration tests you need to explicitly specify path to JDK 8, which 
will be used to spawn mock Presto instance.

Run tests with command:

```
 mvn test -Djava8.home=/usr/lib/jvm/java-8-oracle
```

Replace value of `java8.home` property with path valid on your system.

## Artifacts

Build produces single library artifact `presto-jdbc-java6-<version>.jar`.
Artifact does not depend on any external libraries.

After build, library will be available in `~/.m2/repository/com/facebook/presto/presto-jdbc-java6`.

Currently we do not publish artifacts from this project to central maven repository.


