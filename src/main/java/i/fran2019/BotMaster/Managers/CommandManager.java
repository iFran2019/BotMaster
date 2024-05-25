package i.fran2019.BotMaster.Managers;

import i.fran2019.BotMaster.API.annotations.CommandOption;
import i.fran2019.BotMaster.API.implementations.Command;
import i.fran2019.BotMaster.BotMaster;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.lang.reflect.Field;
import java.util.*;

public class CommandManager extends ListenerAdapter {
    private boolean started = false;

    private final Map<String, Command> commands;
    private final List<CommandData> commandsData;

    public CommandManager() {
        BotMaster.getBotMaster().getJda().addEventListener(this);

        this.commands = new HashMap<>();
        this.commandsData = new ArrayList<>();
    }

    public void registerCommand(Command cmd) {
        if (this.commands.get(cmd.getName()) == null) {
            this.commands.put(cmd.getName(), cmd);
            addCommandData(cmd);
        }
        else BotMaster.getLogger().warn("The command cannot be registered correctly because it already exists.");

        if (started) registerSlashCommands();
    }

    protected void registerSlashCommands() {
        if (!this.started) this.started = true;

        if (BotMaster.getBotMaster().getConfigManager().COMMANDS_SLASH_REGISTER.equalsIgnoreCase("global")) {
            BotMaster.getLogger().info("Loading commands. (Global) (takes 1 hour to update)");
            BotMaster.getBotMaster().getJda().retrieveCommands().queue(existingCommands -> {
                existingCommands.forEach(existingCommand -> {
                    for (CommandData commandData : commandsData) {
                        if (commandData.getName().equals(existingCommand.getName())) {
                            BotMaster.getBotMaster().getJda().deleteCommandById(existingCommand.getIdLong()).queue();
                        }
                    }
                });
                BotMaster.getBotMaster().getJda().updateCommands().addCommands(this.commandsData).queue();
            });
        } else {
            BotMaster.getLogger().info("Loading commands on {} guilds. (Local)", BotMaster.getBotMaster().getJda().getGuilds().size());
            for (Guild guild : BotMaster.getBotMaster().getJda().getGuilds()) {
                guild.retrieveCommands().queue(existingCommands -> {
                    existingCommands.forEach(existingCommand -> {
                        for (CommandData commandData : commandsData) {
                            if (commandData.getName().equals(existingCommand.getName())) {
                                guild.deleteCommandById(existingCommand.getIdLong()).queue();
                            }
                        }
                    });
                    guild.updateCommands().addCommands(this.commandsData).queue();
                });
            }
        }
    }

    private void addCommandData(Command cmd) {
        CommandDataImpl commandData = new CommandDataImpl(cmd.getName(), "Command description");
        for (Field field : cmd.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(CommandOption.class)) {
                CommandOption option = field.getAnnotation(CommandOption.class);
                OptionData optionData = new OptionData(option.type(), option.name().toLowerCase(), option.description().toLowerCase(), option.required());
                for (String choice : option.choices()) {
                    if (choice.contains("|")) {
                        String[] choices = choice.split("\\|");
                        optionData.addChoice(choices[0], choices[1]);
                    } else optionData.addChoice(choice, choice);
                }
                commandData.addOptions(optionData);
            }
        }
        this.commandsData.add(commandData);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(this.commandsData).queue();;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        String commandName = e.getName();
        Command command = this.commands.get(commandName);
        if (command != null) {
            BotMaster.getLogger().debug("Command Name: {}", commandName);
            for (Field field : command.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(CommandOption.class)) {
                    CommandOption option = field.getAnnotation(CommandOption.class);
                    OptionMapping optionMapping = e.getOption(option.name().toLowerCase());
                    if (optionMapping != null) {
                        try {
                            field.setAccessible(true);
                            field.set(command, optionMapping.getAsString());
                        } catch (IllegalAccessException ex) {
                            BotMaster.getLogger().error("Error executing command: occurred while accessing field.");
                        }
                    }
                }
            }
            command.onExecute(e);
        } else {
            BotMaster.getLogger().warn("Command not found: {}", commandName);
        }
    }
}
