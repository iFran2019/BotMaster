package i.fran2019.BotMaster.API.implementations;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Command {
    @Getter String name;

    public Command(String name) {
        this.name = name;
    }

    public void onExecute(SlashCommandInteractionEvent e) {

    }
}
