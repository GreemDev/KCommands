package net.greemdev.kcommands

import java.awt.Color

/**
 * Class containing what would otherwise be constant values.
 */
class SlashCommandClientConfig {

    companion object {
        @JvmStatic infix fun of(initializer: SlashCommandClientConfig.() -> Unit) = SlashCommandClientConfig().apply(initializer)
        @JvmStatic infix fun justCommands(commands: Set<SlashCommand>) = SlashCommandClientConfig of {
            this.commands = commands
        }
    }

    var commands: Set<SlashCommand> = hashSetOf()

    var checkFailedColor: Color = Color.RED
    var checkFailedTitle: String = "Check failed"


}