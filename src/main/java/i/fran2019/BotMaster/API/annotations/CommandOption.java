package i.fran2019.BotMaster.API.annotations;

import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandOption {
    String name();
    String description();
    boolean required() default false;
    OptionType type() default OptionType.STRING;
    String[] choices() default {};
}
