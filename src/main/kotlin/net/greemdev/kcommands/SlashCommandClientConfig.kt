package net.greemdev.kcommands

import java.awt.Color
import net.greemdev.kcommands.obj.SlashCommandCheck

/**
 * Class containing what would otherwise be constant values.
 */
class SlashCommandClientConfig private constructor() {

    companion object {
        @JvmStatic fun default() = SlashCommandClientConfig()
        @JvmStatic infix fun of(initializer: SlashCommandClientConfig.() -> Unit) = default().apply(initializer)
        @JvmStatic infix fun justCommands(commands: Set<SlashCommand>) = default().commands(commands)
    }

    fun commands(coll: Collection<SlashCommand>): SlashCommandClientConfig {
        commands = coll.toHashSet()
        return this
    }

    /**
     * The collection (specifically [Set]) containing all of the [SlashCommand] or [GuildSlashCommand] objects to be handled by this [SlashCommandClient].
     */
    var commands: Set<SlashCommand> = hashSetOf()

    /**
     * The color of the embed sent when one or more [SlashCommandCheck]s result in a [false] value.
     */
    @Suppress("KDocUnresolvedReference")
    var checksFailedColor: Color = Color.RED

    /**
     * The title of the embed sent when one or more [SlashCommandCheck]s result in a [false] value.
     */
    @Suppress("KDocUnresolvedReference")
    var checksFailedTitle: String = "One or more checks failed"

    /**
     * The line format used on each [SlashCommandCheck] that failed when a command is called.
     * The value [{}] gets replaced with the failed check's reason.
     */
    var checksFailedLineFormat: String = " - {}"
}