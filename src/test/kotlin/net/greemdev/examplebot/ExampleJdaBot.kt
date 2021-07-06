package net.greemdev.examplebot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.greemdev.kcommands.SlashCommand
import net.greemdev.kcommands.SlashCommandClient
import net.greemdev.kcommands.ext.ButtonComponentId
import net.greemdev.kcommands.ext.actionRowsFrom
import net.greemdev.kcommands.obj.ButtonClickContext
import net.greemdev.kcommands.obj.SlashCommandContext
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
            val id = newButtonId()
                .user(ctx.userId())
                .action("delete")
            danger(id, "Delete")
            // Note the use of 'newButtonId', returning a value of 'ButtonComponentId'.
            // With this library, button component IDs follow a pattern: commandName:userId:action:value.
            // commandName is used to filter command events into specific SlashCommand handleButtonClick functions.
            // userId is to prevent spoofing.
            // action is which action the button is doing; note above it's '.action("delete")' because the action is delete.
            // in the button click handler we'll check the action.
            // Value is needed in some small cases; an example being a purge command like shown in the JDA examples.
        }

        // Checks are tested against when a SlashCommandEvent is received and automatically replied to with the failureReason when it fails.
        // If any checks return true on a command, handleSlashCommand is not called.

        // This check is literally useless; but you get the point, yes?
        checks {
            check("User is a bot.") { ctx ->
                !ctx.user().isBot
            }
        }
    }

    override fun handleSlashCommand(context: SlashCommandContext) {
        // Because the option was created above as required, we can get the option without a problem.
        // If the option was optional, we'd do getOptionElse(name, nullableDefaultValue)
        // so you can check against a default value in the event the option was not present.
        val content = context.getOptionValue<String>("content")
        // New receiver function allowing the end-user to do everything they need to reply to a command in a lambda; automatically queueing the RestAction.
        context.replyWithButtonsAsync {
            embed {
                description(content)
                color(context.member()?.color ?: Color.MAGENTA)
            }
        }


        /*
        // This reply function has access to a KEmbedBuilder receiver function.
        // This object's creation syntax is especially neat.
        context.reply {
            description(content)
            color(context.member()?.color ?: Color.MAGENTA)
        }
            // This function call adds the buttons defined in the constructor above; generating them using the value of context.
            .actionRowsFrom(context)
            .queue()*/
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