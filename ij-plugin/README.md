# IntelliJ Plugin for Task Tracking and IDE Management

This plugin is a comprehensive tool for tracking user activities, managing IDE settings, and collecting data during experiments or tasks in IntelliJ-based IDEs.

## Features

### Data Collection
The plugin collects a wide range of data using various listeners within the IntelliJ SDK platform and operating system tools:

- **Code Changes**: Captures snapshots of all code modifications.
- **IDE Activities**: Tracks all activities that occur in the IDE. More details about activity types can be found [here](src/main/kotlin/org/jetbrains/research/tasktracker/tracking/activity/ActivityEvent.kt).
- **Window Navigation**: Records switching between file windows, tool windows, and IDE plugin windows.
- **User Feedback**: Collects survey responses.
- **External Data**: Gathers third-party logs/files specified in the [configuration](src/main/kotlin/org/jetbrains/research/tasktracker/config/content/PluginInfoConfig.kt) according to this [structure](src/main/kotlin/org/jetbrains/research/tasktracker/config/content/Log.kt).

### IDE Settings Management
The plugin provides powerful capabilities to control various IDE settings:

- **Code Completion**: Enable or disable auto-completion features.
- **Theme Management**: Change the IDE color scheme.
- **Inspection Settings**: Modify code inspection configurations.

These settings can be applied globally for the entire experiment, for specific tasks, or for segments of tasks.

## Configuration System

The plugin leverages a **powerful and flexible configuration file system** that enables complete customization of your experiments. **Detailed documentation about the configuration files can be found [here](src/main/resources/org/jetbrains/research/tasktracker/config/)**.

This robust configuration system empowers you to:
- **Design custom task scenarios** with precise control over task sequence and presentation
- **Fine-tune IDE settings** to create the perfect environment for each experiment
- **Craft interactive surveys** with various question types to gather valuable feedback
- **Configure code inspections** to guide participants or challenge them appropriately
- **Customize experiment flow** with agreements, information pages, and completion messages
- **And much more!**

The configuration system is designed to be intuitive yet comprehensive, allowing both simple setups and complex experimental designs.

## Server Connection

The plugin works exclusively with a server whose address must be specified in the [domain.properties](src/main/resources/properties/actual/domain.properties) file. For example:

```
serverAddress=http://0.0.0.0:8080
```

The settings for plugin configuration and server interaction are stored in the [properties directory](src/main/resources/properties/actual). If this directory does not exist, it will be created with default properties and values.

## Extensibility

While the plugin is not primarily designed for modifying source code, it can be extended to add new tracking methods and system management capabilities.

## Getting Started

### Developer Mode

Clone the repository and build the project:

```text
./gradlew build
```

To run the plugin, execute the `runIde` IntelliJ task:

```text
./gradlew runIde
```

You can use the run IDE plugin configuration: [configuration file](../.run/Run%20IDE%20with%20Plugin.run.xml).
