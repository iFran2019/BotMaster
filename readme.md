# BotMaster

BotMaster is open-source software that enables the creation of modular bots easily and efficiently. Additionally, it is designed to avoid excessive resource consumption, ensuring optimal performance.

Available on BuiltByBit [click here](https://builtbybit.com/resources/botmaster.58368).

## Features

- **Modularity**: Allows easy addition and removal of modules (plugins).
- **Automatic Registration of Slash Commands**: Automates the registration of slash commands in Discord servers.
- **Resource Efficiency**: Designed to avoid excessive resource consumption, ensuring optimal and efficient performance.
- **Wiki**: A wiki as complete as possible [click here](https://github.com/iFran2019/BotMaster/wiki).

## TODO List

- [ ] Add button component handler.
- [X] Add redis support.
- [ ] Add mysql support.

## Usage

BotMaster automatically creates a plugins folder and registers slash commands neatly in the servers. You can choose between two modes of slash command registration:

1. **Global**: This mode registers the commands globally. Although it may take up to 1 hour to be fully functional, these commands will be accessible in all Discord servers.

2. **Local**: This mode registers the commands server by server. It is ideal for testing or for bots that operate in a few servers. It is not recommended for bots in more than 200 servers due to the time and effort required to update commands in each server individually.

## Contributions

Contributions are welcome. Please open an issue or a pull request on GitHub if you wish to contribute to the project.

## Support

If you find BotMaster useful and would like to support its development, consider donating [click here](https://buymeacoffee.com/ifran2019).

## Dependencies

BotMaster uses several dependencies to ensure its functionality. Here are the main dependencies used in the project:

- [SnakeYAML](https://mvnrepository.com/artifact/org.yaml/snakeyaml/2.0): YAML parsing and emitting library.
- [SLF4J API](https://mvnrepository.com/artifact/org.slf4j/slf4j-api/1.7.32): Simple Logging Facade for Java.
- [JDA (Java Discord API)](https://mvnrepository.com/artifact/net.dv8tion/JDA/5.0.0-beta.24): Java wrapper for the popular chat & VOIP service: Discord.
- [Apache Commons IO](https://mvnrepository.com/artifact/commons-io/commons-io/2.11.0): Library of utilities to assist with developing IO functionality.
- [MongoDB Driver Sync](https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync/4.8.2): The MongoDB synchronous driver.
- [Lombok](https://mvnrepository.com/artifact/org.projectlombok/lombok/1.18.32): A java library that automatically plugs into your editor and build tools, spicing up your Java.

For more information, check the complete documentation or contact the project maintainers on GitHub.