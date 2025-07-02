package i.fran2019.BotMaster.API.implementations;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.ArrayList;
import java.util.List;

public class Command {
    @Getter String name;
    @Getter IntegrationType[] integrationTypes;
    @Getter List<InteractionContextType> interactionContextType;

    public Command(String name) {
        this.name = name;
        this.integrationTypes = new IntegrationType[]{ IntegrationType.GUILD_INSTALL };
        this.interactionContextType = new ArrayList<>();
        this.interactionContextType.addAll(InteractionContextType.ALL);

    }

    public Command(String name, IntegrationType[] integrationTypes) {
        this.name = name;
        this.integrationTypes = integrationTypes;
        this.interactionContextType = new ArrayList<>();
        this.interactionContextType.addAll(InteractionContextType.ALL);
    }

    public Command(String name, IntegrationType[] integrationTypes, List<InteractionContextType> interactionContextType) {
        this.name = name;
        this.integrationTypes = integrationTypes;
        this.interactionContextType = interactionContextType;
    }

    public void onExecute(SlashCommandInteractionEvent e) {

    }
}
