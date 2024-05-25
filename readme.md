# BotMaster

BotMaster is open-source software that enables the creation of modular bots easily and efficiently. Additionally, it is designed to avoid excessive resource consumption, ensuring optimal performance.

## Features

- **Modularity**: Allows easy addition and removal of modules (plugins).
- **Automatic Registration of Slash Commands**: Automates the registration of slash commands in Discord servers.
- **Modes of Slash Commands**:
    - **Global**: Commands are registered globally, which may take up to 1 hour but makes them functional across all servers.
    - **Local**: Commands are registered server by server, suitable for a few servers but not recommended for more than 200 servers.
- **Resource Efficiency**: Designed to avoid excessive resource consumption, ensuring optimal and efficient performance.

## TODO List

- [ ] Improve documentation for plugin creation.
- [ ] Add button component handler.
- [ ] Add plugin examples to facilitate understanding.
- [ ] Add redis support.
- [ ] Add mysql support.

## Usage

BotMaster automatically creates a plugins folder and registers slash commands neatly in the servers. You can choose between two modes of slash command registration:

1. **Global**: This mode registers the commands globally. Although it may take up to 1 hour to be fully functional, these commands will be accessible in all Discord servers.

2. **Local**: This mode registers the commands server by server. It is ideal for testing or for bots that operate in a few servers. It is not recommended for bots in more than 200 servers due to the time and effort required to update commands in each server individually.

## Contributions

Contributions are welcome. Please open an issue or a pull request on GitHub if you wish to contribute to the project.

## Support

If you find BotMaster useful and would like to support its development, consider donating [here](https://buymeacoffee.com/ifran2019).

---

For more information, check the complete documentation or contact the project maintainers on GitHub.