@file:Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_VARIABLE")

package net.greemdev.examplebot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.requests.GatewayIntent
import net.greemdev.kcommands.SlashCommand
import net.greemdev.kcommands.SlashCommandClient
import net.greemdev.kcommands.*
import net.greemdev.kcommands.ButtonClickContext
import net.greemdev.kcommands.SlashCommandContext
import java.awt.Color

class ExampleJdaBot {

    lateinit var jda: JDA

    companion object {
        @JvmStatic val slashCommands = hashSetOf(
            SayCommand()
        )
        @JvmStatic fun main(args: Array<out String>) {
            ExampleJdaBot().start(args.first())
        }
    }

    fun start(token: String) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(SlashCommandClient.get(slashCommands))
            .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)) //idc about intents in this example tbh
            .build().awaitReady()
    }

}

class SayCommand : SlashCommand("say", "Bot repeats what you tell it to.") {
    // Here in the constructor, we add options and buttons.
    // You can also add 'checks', which are custom bits of code that return booleans that are checked against the Slash Command event.
    // Examples of all 3 shown below
    init {
        options {
            // option creation functions follow this same format, so for example an optional user option would be optionalUser.
            requiredString("content", "What to say")
        }

        // This is where it gets weird.
        buttons { ctx -> // Because buttons are only needed when the command is being executed, you have access to the SlashCommandContext
                         // that triggered it allowing usage of data that is only accessible when a command is run inside the component ID.

            var id = newButtonId()
                .user(ctx.userId())
                .action("delete")

            //Alternatively:
            id = buttonId {
                user(ctx.userId())
                action("delete")
            }

            danger(id, "Delete")
            /*  Note the use of 'newButtonId', returning a value of 'ButtonComponentId'.
                With this library, button component IDs follow a pattern: commandName:userId:action:value.

                    commandName is used to filter command events into specific SlashCommand handleButtonClick functions.
                    userId is to prevent spoofing.
                    action is which action the button is doing; note above it's '.action("delete")' because the action is delete.
                        In the button click handler we'll check the action.
                    value is needed in some small cases; an example being a purge command like shown in the JDA examples.
            */
        }

        // Checks are tested against when a SlashCommandEvent is received and automatically replied to with the failureReason when it fails.
        // If any checks return true on a command, handleSlashCommand is not called.

        // This check is literally useless; but you get the point, yes?
        checks {
            // The string is the failure reason, aka, the string should be the opposite of what you're checking for.
            // For example, in this check, we're ensuring that anyone who executes the command is *not* a bot.
            // Therefore, if the check were to fail, it's because the user *is* a bot.
            check("User is a bot.") { ctx ->
                !ctx.user().isBot
            }

            // The library also provides rather basic premade checks:
            requireUserAdministrator()
            requireUserApplicationOwner()
            requireUserPermission(Permission.MANAGE_SERVER, Permission.MESSAGE_WRITE)
        }
    }

    override fun handleSlashCommand(context: SlashCommandContext) {
        // Because the option was created above as required, we can get the option without a problem.
        val content = context.getOptionValue<String>("content")

        // If the option was registered as optional, you'll need to provide a default, nullable value;
        val content2 = context.getOptionElse("content", "")

        // If you'd rather interact with the JDA OptionMapping object itself, you can use option(String)
        val content3 = context.option("content")?.asString

        // Because this is an example, we're going to have multiple uncommented statements that all achieve the same end goal;
        // showing you the different ways you can approach how you design your commands.

        ///----------------------------1: As little repeated code as possible--------------------------------------

        context.replyWithButtonsAsync {

            modifier {
                ephemeral()
            }

            embed {
                description(content)
                color(context.member()?.color ?: Color.MAGENTA)
            }
        }

        ///----------------------------2: Standard JDA RestAction chaining--------------------------------------\

        context.reply {
            description(content)
            color(context.member()?.color ?: Color.MAGENTA)
        }
            // This function call adds the buttons defined in the constructor above; generating them using the value of context.
            .actionRowsFrom(context)
            .ephemeral()
            .queue()

        ///----------------------------3: A little more repeated code than #1 I guess--------------------------------------

        context.replyAsync {
            buttons(context)

            modifier {
                ephemeral()
            }

            embed {
                description(content)
                color(context.member()?.color ?: Color.MAGENTA)
            }
        }
    }

    override fun handleButtonClick(context: ButtonClickContext) {
        val id = context.buttonId() //this is a ButtonComponentId
        if (id.user() != context.user().id) return
        when (id.action()) {
            // This is where the button's ButtonComponentId#action(Any) call comes to use
            "delete" -> context.ack().queue { context.message()?.delete()?.queue() }
        }
    }
}