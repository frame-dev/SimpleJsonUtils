# SimpleJsonUtils

This is a Project for simple json serialization and deserialization with option for nested lists/map/classes(object)
<br>
There are flags available for debugging and pretty printing.

### Usage
``` java
# No pretty printing no debugging
JsonParser jsonParser = new JsonParser();

# for pretty printing
JsonParser jsonParser = new JsonParser(Flag.PRETTY_PRINT);
# or
JsonParser jsonParser = new JsonParser();
jsonParser.setIntend(true);

# for debugging
JsonParser jsonParser = new JsonParser(Flag.DEBUG);
# or
JsonParser jsonParser = new JsonParser();
jsonParser.setDebug(true);

# for both debugging and pretty printing
JsonParser jsonParser = new JsonParser(Flag.PRETTY_PRINT, Flag.DEBUG);
# or
JsonParser jsonParser = new JsonParser();
jsonParser.setIntend(true);
jsonParser.setDebug(true);
```
for object to string use
``` java
# Serialize object
jsonParser.serializeObject(object);
```
``` java
# deserialize object
jsonParser.deserializeObject(object, class);
```
for object to file use
``` java
# serialize object to file
jsonParser.saveToFile(file, object);
```

``` java
# deserialize object from file
jsonParser.loadFromFile(file, class);
```

## Repository
### Maven
``` xml
<repositories>
    <repository>
        <id>framedev-repository</id>
        <url>https://repository.framedev.ch:444/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>ch.framedev</groupId>
        <artifactId>SimpleJsonUtils</artifactId>
        <version>1.0.2.3-RELEASE</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
### Gradle Groovy
``` groovy
maven {
    url "https://repository.framedev.ch:444/releases"
}

implementation "ch.framedev:SimpleJsonUtils:1.0.2.3-RELEASE"
```

### Gradle Kotlin
``` kotlin
maven {
    name = "framedevRepositoryReleases"
    url = uri("https://repository.framedev.ch:444/releases")
}

implementation("ch.framedev:SimpleJsonUtils:1.0.2.3-RELEASE")
```