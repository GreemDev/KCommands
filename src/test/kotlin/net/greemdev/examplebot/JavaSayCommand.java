package net.greemdev.examplebot;

import kotlin.Unit;
import net.greemdev.kcommands.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings("ConstantConditions")
public class JavaSayCommand extends SlashCommand {

    public JavaSayCommand() {
        super("say", "Bot repeats what you tell it to");

        // Basic Java interop API

        options(Java.interop().consumer(scope -> {
            scope.requiredString("content", "What to say", Java.interop().noop());
        }));

        components(Java.interop().consumer(components -> {
            components.buttons(Java.interop().biConsumer((buttons, ctx) -> {
                ComponentId id = newComponentId()
                        .user(ctx.userId())
                        .action("delete");
                buttons.danger(id, "Danger", null);
            }));
        }));

        checks(Java.interop().consumer(scope -> scope.check("User is a bot", (ctx) -> !ctx.user().isBot())));

        // using Kotlin FunctionX<> classes directly

        options(scope -> {
            scope.requiredString("content", "What to say", (__) -> Unit.INSTANCE);
            return Unit.INSTANCE;
        });

        components(scope -> {
            scope.buttons((scope2, ctx) -> {
                ComponentId id = newComponentId()
                        .user(ctx.userId())
                        .action("delete");
                scope2.danger(id, "Danger", null);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });

        checks(scope -> {
            scope.check("User is a bot",
                    (ctx) -> !ctx.user().isBot());

            return Unit.INSTANCE;
        });
    }

    @Override public @NotNull SlashCommandResult handleSlashCommand(@NotNull SlashCommandContext context) {
        String content = context.option("content").getAsString(); //java doesnt like my getOptionValue functions
        return context.result()
                .withEmbed(Java.interop().kEmbed(embed -> {
                    embed.description(content);
                    embed.color(Color.GREEN);
                }))
                .withEmbed(embed -> {
                    embed.description(content);
                    embed.color(Color.GREEN);
                    return Unit.INSTANCE;
                })
                .ephemeral()
                .notEphemeral()
                .withAllCommandComponents();
    }

    @Override public void handleButtonClick(@NotNull ButtonClickContext context) {
        ComponentId id = context.buttonId();
        if (!context.user().getId().equals(id.user())) return;
        if ("delete".equals(id.action())) {
            context.ack().queue(v -> context.message().delete().queue());
        }
    }
}
