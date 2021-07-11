@file:Suppress("KDocUnresolvedReference")

package net.greemdev.kcommands

import java.awt.Color
import java.lang.reflect.Constructor
import kotlin.reflect.KClass

/**
 * Class containing what would otherwise be constant values; configurable when creating your [SlashCommandClient].
 */
@Suppress("DataClassPrivateConstructor")
data class SlashCommandClientConfig private constructor(private var commands: Set<SlashCommand> = hashSetOf()) {

    companion object {
        @JvmStatic
        fun default() = SlashCommandClientConfig()
        @JvmStatic
        infix fun of(initializer: SlashCommandClientConfig.() -> Unit) = default().apply(initializer)
        @JvmStatic
        infix fun justCommands(commands: Collection<SlashCommand>) = default().commands(commands)
        @JvmStatic infix fun justCommandsFromJavaClasses(classes: Collection<Class<SlashCommand>>): SlashCommandClientConfig {
            val commands = classes.mapNotNull { executeElseNull { it.getConstructor() } }
                .map(Constructor<SlashCommand>::newInstance)
                .filterIsInstance<SlashCommand>()
            return default().commands(commands)
        }
        @JvmStatic
        infix fun justCommandsFromClasses(classes: Collection<KClass<SlashCommand>>): SlashCommandClientConfig {
            return justCommandsFromJavaClasses(classes.map { it.java })
        }
    }

    fun commands(coll: Collection<SlashCommand>): SlashCommandClientConfig {
        commands = commands.toHashSet().apply { addAll(coll) }
        return this
    }

    fun commands(): HashSet<SlashCommand> = commands.toHashSet()

    fun commandBy(name: String) = commands().firstOrNull { it.name.equals(name, true) }

    /**
     * The separator used when forming and parsing [ButtonComponentId]s.
     */
    var componentIdSeparator: Char = ':'

    /**
     * The color of the embed sent when one or more [SlashCommandCheck]s result in a [false] value.
     */
    var checksFailedColor: Color = Color.RED

    /**
     * The title of the embed sent when one or more [SlashCommandCheck]s result in a [false] value.
     */
    var checksFailedTitle: String = "One or more checks failed"

    /**
     * The line format used on each [SlashCommandCheck] that failed when a command is called.
     * The value [{}] gets replaced with the failed check's reason.
     */
    var checksFailedLineFormat: String = " - {}"

    /**
     * The failure reason of the default [requireUserApplicationOwner] check.
     */
    var checkUserNotOwner: String = "User is not an owner of the Application."

    /**
     * The failure reason of the default [requireUserAdministrator] check.
     */
    var checkUserNotAdmin: String = "User does not have the Administrator permission."
}