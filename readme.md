# SimpleJsonUtils

**SimpleJsonUtils** is a lightweight Java library for **JSON serialization and deserialization** with support for **nested lists, maps, and objects**.  
It includes **debugging** and **pretty-printing** options for better readability and development insights.

---

## 📖 Table of Contents
- [🚀 Features](#-features)
- [📌 Installation](#-installation)
    - [Maven](#maven)
    - [Gradle Groovy](#gradle-groovy)
    - [Gradle Kotlin](#gradle-kotlin)
- [🔧 Usage](#-usage)
    - [Basic Setup](#basic-setup)
    - [Serialization & Deserialization](#serialization--deserialization)
    - [File Operations](#file-operations)
- [🔍 Debugging & Pretty Printing](#-debugging--pretty-printing)
- [📜 License](#-license)
- [📬 Contact](#-contact)

---

## 🚀 Features
- **Serialize** Java objects into JSON format.
- **Deserialize** JSON back into Java objects.
- **Supports nested objects, lists, and maps**.
- **Pretty-printing** for readable JSON output.
- **Debugging mode** to track serialization processes.

---

## 📌 Installation

### **Maven**
```xml
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
        <version>1.0.3.2-RELEASE</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### **Gradle Groovy**
```groovy
repositories {
    maven {
        url "https://repository.framedev.ch:444/releases"
    }
}

dependencies {
    implementation "ch.framedev:SimpleJsonUtils:1.0.3.2-RELEASE"
}
```

### **Gradle Kotlin**
```kotlin
repositories {
    maven {
        name = "framedevRepositoryReleases"
        url = uri("https://repository.framedev.ch:444/releases")
    }
}

dependencies {
    implementation("ch.framedev:SimpleJsonUtils:1.0.3.2-RELEASE")
}
```

---

## 🔧 Usage

### **Basic Setup**
```java
import ch.framedev.simplejsonutils.JsonParser;
import ch.framedev.simplejsonutils.Flag;

// Default (no pretty-printing, no debugging)
JsonParser jsonParser = new JsonParser();

// Enable pretty-printing
JsonParser jsonParser = new JsonParser(Flag.PRETTY_PRINT);
// or
JsonParser jsonParser = new JsonParser();
jsonParser.setIndent(true);

// Enable debugging
JsonParser jsonParser = new JsonParser(Flag.DEBUG);
// or
JsonParser jsonParser = new JsonParser();
jsonParser.setDebug(true);

// Enable both debugging & pretty-printing
JsonParser jsonParser = new JsonParser(Flag.PRETTY_PRINT, Flag.DEBUG);
// or
JsonParser jsonParser = new JsonParser();
jsonParser.setIndent(true);
jsonParser.setDebug(true);
```

---

### **Serialization & Deserialization**
#### **Convert Object to JSON String**
```java
// Convert an object to a JSON string
String json = jsonParser.serializeObject(myObject);
System.out.println(json);
```

#### **Convert JSON String to Object**
```java
// Convert a JSON string back into an object
MyClass myObject = jsonParser.deserializeObject(jsonString, MyClass.class);
```

---

### **File Operations**
#### **Save Object to File**
```java
import java.io.File;

// Serialize an object and save it to a file
jsonParser.saveToFile(new File("output.json"), myObject);
```

#### **Load Object from File**
```java
import java.io.File;

// Load and deserialize an object from a file
MyClass myObject = jsonParser.loadFromFile(new File("output.json"), MyClass.class);
```

---

## 🔍 Debugging & Pretty Printing
SimpleJsonUtils provides **debug mode** to help track issues during serialization.  
You can also enable **pretty-printing** to format JSON output for better readability.

```java
// Enable debugging and pretty-printing
JsonParser jsonParser = new JsonParser(Flag.PRETTY_PRINT, Flag.DEBUG);
```

---

## 📜 License
This project is licensed under the **GPL-3.0 license**.

---

## 📬 Contact
For questions or contributions, visit [FrameDev Discord](https://discord.gg/BCGz53AQ4M).
