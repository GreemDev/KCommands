<table>
    <tr>
        <td align="center" width="25%">
            <img alt="Kotlin logo" src="https://cdn.freebiesupply.com/logos/large/2x/kotlin-1-logo-png-transparent.png"/>
        </td>
        <td align="center" width="75%">

# KCommands

Another [JDA](https://github.com/DV8FromTheWorld/JDA) Slash Commands creation and execution wrapper.
See [the Kotlin example](https://code.greemdev.net/greem/KCommands/-/blob/main/src/test/kotlin/net/greemdev/examplebot/ExampleJdaBot.kt) to see how you can implement this library.

[![Discord](https://img.shields.io/discord/405806471578648588.svg?color=7000FB&label=discord&style=for-the-badge)](https://discord.gg/H8bcFr2)
        </td>
    </tr>
</table>

### Note:
This library was not made with Java interop in mind. Most of the functions will make you return `Unit.INSTANCE`.
If this does not deter you, you can see basic (comment-less) usage [here](https://code.greemdev.net/greem/KCommands/-/blob/main/src/test/kotlin/net/greemdev/examplebot/JavaSayCommand.java).

## Installation

For both Maven and Gradle, you need to add the following repository: https://mvn.greemdev.net/repository/maven-releases

Maven:

```xml
<dependency>
    <groupId>net.greemdev</groupId>
    <artifactId>KCommands</artifactId>
    <version>VERSION</version>
</dependency>

<repository>
    <id>greemdev</id>
    <url>https://mvn.greemdev.net/repository/maven-releases</url>
</repository>
```

Gradle:
```groovy
dependencies {
    implementation("net.greemdev:KCommands:VERSION")
}

repositories {
    maven("https://mvn.greemdev.net/repository/maven-releases/")
}
```
