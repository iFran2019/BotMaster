package i.fran2019.BotMaster.Managers;

import i.fran2019.BotMaster.API.annotations.CommandOption;
import i.fran2019.BotMaster.API.implementations.Command;
import i.fran2019.BotMaster.BotMaster;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandManager extends ListenerAdapter {
    private boolean started = false;

    private final Map<String, Command> commands;
    private final List<CommandData> commandsData;

    public CommandManager() {
        BotMaster.getBotMaster().getJda().addEventListener(this);

        this.commands = new HashMap<>();
        this.commandsData = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public void registerCommand(Command cmd) {
        if (started) {
            BotMaster.getLogger().error("Can't register a new command.");
            return;
        }

        if (this.commands.get(cmd.getName().toLowerCase()) == null) {
            this.commands.put(cmd.getName().toLowerCase(), cmd);
            addCommandData(cmd);
        }
        else BotMaster.getLogger().warn("The command cannot be registered correctly because it already exists.");

    }

    protected void registerSlashCommands() {
        if (!this.started) this.started = true;

        if (BotMaster.getBotMaster().getConfigManager().COMMANDS_SLASH_ENABLED) return;

        if (BotMaster.getBotMaster().getConfigManager().COMMANDS_SLASH_REGISTER.equalsIgnoreCase("global")) {
            BotMaster.getLogger().info("Loading commands. (Global) (takes 1 hour to update)");

            BotMaster.getBotMaster().getJda().retrieveCommands().queue(existingCommands -> {
                if (existingCommands != null && commandsData != null) {
                    List<Long> commandsToDelete = existingCommands.stream()
                            .filter(existingCommand -> commandsData.stream().noneMatch(commandData -> commandData.getName().equals(existingCommand.getName())))
                            .map(ISnowflake::getIdLong)
                            .toList();

                    if (!commandsToDelete.isEmpty()) {
                        CompletableFuture<Void> deletionFuture = CompletableFuture.allOf(
                                commandsToDelete.stream()
                                        .map(commandId -> CompletableFuture.runAsync(() ->
                                                BotMaster.getBotMaster().getJda().deleteCommandById(commandId).queue(
                                                        success -> {},
                                                        throwable -> {}
                                                )))
                                        .toArray(CompletableFuture[]::new)
                        );

                        deletionFuture.thenRun(() ->
                                BotMaster.getBotMaster().getJda().updateCommands().addCommands(this.commandsData).queue(
                                        success -> BotMaster.getLogger().info("Global commands registered successfully."),
                                        failure -> BotMaster.getLogger().error("Failed to register global commands.", failure)
                                )
                        );
                    } else {
                        BotMaster.getBotMaster().getJda().updateCommands().addCommands(this.commandsData).queue(
                                success -> BotMaster.getLogger().info("Global commands registered successfully."),
                                failure -> BotMaster.getLogger().error("Failed to register global commands.", failure)
                        );
                    }
                }
            });

        } else {
            BotMaster.getLogger().info("Loading commands on {} guilds. (Local)", BotMaster.getBotMaster().getJda().getGuilds().size());

            if (BotMaster.getBotMaster().getJda().getGuilds().size() >= 50) {
                BotMaster.getLogger().warn("You could consider starting to use \"Global\" commands to avoid a high performance load when starting the bot.");
            }

            for (Guild guild : BotMaster.getBotMaster().getJda().getGuilds()) {
                guild.retrieveCommands().queue(existingCommands -> {
                    if (existingCommands != null && commandsData != null) {
                        List<Long> commandsToDelete = existingCommands.stream()
                                .filter(existingCommand -> commandsData.stream().noneMatch(commandData -> commandData.getName().equals(existingCommand.getName())))
                                .map(ISnowflake::getIdLong)
                                .toList();

                        if (!commandsToDelete.isEmpty()) {
                            CompletableFuture<Void> deletionFuture = CompletableFuture.allOf(
                                    commandsToDelete.stream()
                                            .map(commandId -> CompletableFuture.runAsync(() ->
                                                    guild.deleteCommandById(commandId).queue(
                                                            success -> {},
                                                            throwable -> {}
                                                    )))
                                            .toArray(CompletableFuture[]::new)
                            );

                            deletionFuture.thenRun(() ->
                                    guild.updateCommands().addCommands(this.commandsData).queue(
                                            success -> BotMaster.getLogger().info("Commands registered successfully on guild: {}", guild.getName()),
                                            failure -> BotMaster.getLogger().error("Failed to register commands on guild: {}", guild.getName(), failure)
                                    )
                            );
                        } else {
                            guild.updateCommands().addCommands(this.commandsData).queue(
                                    success -> BotMaster.getLogger().info("Commands registered successfully on guild: {}", guild.getName()),
                                    failure -> BotMaster.getLogger().error("Failed to register commands on guild: {}", guild.getName(), failure)
                            );
                        }
                    }
                });
            }
        }
    }

    private void addCommandData(Command cmd) {
        String[] cmdSplit = cmd.getName().toLowerCase().split(" ");
        CommandDataImpl commandData = new CommandDataImpl(cmdSplit[0], cmdSplit[0]);
        SubcommandGroupData subcommandGroupData = null;
        SubcommandData subcommandData = null;

        if (cmdSplit.length == 3) {
            subcommandGroupData = new SubcommandGroupData(cmdSplit[1], cmdSplit[1]);
            subcommandData = new SubcommandData(cmdSplit[1], cmdSplit[1]);
        } else if (cmdSplit.length == 2) subcommandData = new SubcommandData(cmdSplit[1], cmdSplit[1]);
        else {
            if (cmdSplit.length != 1) {
                BotMaster.getLogger().info("Error on saving command Data");
                return;
            }
        }

        for (Field field : cmd.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(CommandOption.class)) {
                CommandOption option = field.getAnnotation(CommandOption.class);
                OptionData optionData = new OptionData(option.type(), option.name().toLowerCase(), option.description().toLowerCase(), option.required());
                for (String choice : option.choices()) {
                    if (choice.contains("|")) {
                        String[] choices = choice.split("\\|");
                        optionData.addChoice(choices[0], choices[1]);
                    } else {
                        optionData.addChoice(choice, choice);
                    }
                }

                if (cmdSplit.length == 3) subcommandData.addOptions(optionData);
                else if (cmdSplit.length == 2) subcommandData.addOptions(optionData);
                else commandData.addOptions(optionData);
            }
        }

        if (cmdSplit.length == 3) {
            subcommandGroupData.addSubcommands(subcommandData);
            commandData.addSubcommandGroups(subcommandGroupData);
        } else if (cmdSplit.length == 2) commandData.addSubcommands(subcommandData);

        this.commandsData.add(commandData);
    }


    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        if (BotMaster.getBotMaster().getConfigManager().COMMANDS_SLASH_ENABLED) {
            if (BotMaster.getBotMaster().getConfigManager().COMMANDS_SLASH_REGISTER.equalsIgnoreCase("Local")) {
                e.getGuild().updateCommands().addCommands(this.commandsData).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        String commandName = e.getName();

        if (e.getSubcommandGroup() != null) commandName = commandName+" "+e.getSubcommandGroup()+" "+e.getSubcommandName();
        else if (e.getSubcommandName() != null) commandName = commandName+" "+e.getSubcommandName();

        Command command = this.commands.get(commandName);
        if (command != null) {
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
