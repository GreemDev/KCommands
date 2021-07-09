package net.greemdev.examplebot;

import kotlin.Unit;
import net.greemdev.kcommands.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings("ConstantConditions")
public class JavaSayCommand extends SlashCommand {

    public JavaSayCommand() {
        super("say", "Bot repeats what you tell it to");

        options(scope -> {
            scope.requiredString("content", "What to say", (__) -> Unit.INSTANCE);
            return Unit.INSTANCE;
        });

        buttons((scope, ctx) -> {
            ButtonComponentId id = newButtonId()
                    .user(ctx.userId())
                    .action("delete");
            scope.danger(id, "Danger", null);
            return Unit.INSTANCE;
        });

        checks(scope -> {
            scope.check("User is a bot",
                    (ctx) -> !ctx.user().isBot());

            return Unit.INSTANCE;
        });

    }

    @Override
    public void handleSlashCommand(@NotNull SlashCommandContext context) {
        String content = context.option("content").getAsString(); //java doesnt like my getOptionValue functions

        context.replyWithButtonsAsync(scope -> {
            scope.embed(k -> {
                k.description(content);
                k.color(Color.GREEN);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
    }

    @Override
    public void handleButtonClick(@NotNull ButtonClickContext context) {
        ButtonComponentId id = context.buttonId();
        if (!context.user().getId().equals(id.user())) return;
        if ("delete".equals(id.action())) {
            context.ack().queue(v -> context.message().delete().queue());
        }
    }
}
