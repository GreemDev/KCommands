package net.greemdev.kcommands

import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import net.greemdev.kcommands.ext.*
import net.greemdev.kcommands.obj.ButtonClickContext
import net.greemdev.kcommands.obj.SlashCommandCheck
import net.greemdev.kcommands.obj.SlashCommandContext

/**
 * The base class for all Slash Commands. This provides a high-level API for creating and executing Slash Commands;
 * along with components, specifically buttons.
 */
abstract class SlashCommand(val name: String, val description: String) {

    var options: Array<OptionData> = arrayOf()
    var checks: Array<SlashCommandCheck> = arrayOf()

    fun buttons(context: SlashCommandContext): Array<Button> = CreationScopes.Headless.buttons().apply { internalLazyButtons(context) }.buttons.toTypedArray()

    private var internalLazyButtons: HeadlessApplicationCommandButtonCreationScope.(SlashCommandContext) -> Unit = { }

    /**
     * Configure the options for use with this command, for use on initialization to upsert commands.
     */
    fun options(func: HeadlessApplicationCommandOptionCreationScope.() -> Unit) {
        options = buildOptions(func)
    }

    /**
     * Configure the buttons for use when this command's result is being sent;
     * with the value of the [SlashCommandContext] passed as a parameter to utilize event-time data in Component IDs.
     */
    fun buttons(func: HeadlessApplicationCommandButtonCreationScope.(SlashCommandContext) -> Unit) {
        internalLazyButtons = func
    }

    /**
     * Configure the checks for use with the [SlashCommandClient] to check against for execution.
     * Any of the provided predicates returning [false] will prevent execution and reply with an error and the check's [SlashCommandCheck.reason].
     */
    @Suppress("KDocUnresolvedReference")
    fun checks(func: HeadlessApplicationCommandCheckCreationScope.() -> Unit) {
        checks = CreationScopes.Headless.checks().apply(func).checks.toTypedArray()
    }

    /**
     * Creates a new [ButtonComponentId] with `commandName`, its first piece, set as the current command's name.
     */
    fun newButtonId() = ButtonComponentId.from(this)

    /**
     * Creates a new [ButtonComponentId] from [newButtonId] and then applies [func] to it allowing for receiver creation of [ButtonComponentId]s.
     */
    fun buttonId(func: ButtonComponentId.() -> Unit) = newButtonId().apply(func)

    /**
     * The function called when this command has been invoked from Discord.
     * You need to ack the request within 3 seconds via [SlashCommandContext.ack] or [SlashCommandContext.reply].
     * Because this function is [abstract], when extending this class you need to override this.
     */
    @Suppress("KDocUnresolvedReference")
    abstract fun handleSlashCommand(context: SlashCommandContext)

    /**
     * The function called when buttons on interactions made from this command have been clicked.
     * (This means the component ID begins with the name provided when creating this class.)
     * You need to ack the request within 3 seconds via [ButtonClickContext.ack], [ButtonClickContext.reply], [ButtonClickContext.editMessage]-type functions.
     * Because this function is [open], when extending this class you do not need to override this; unless you want to utilize Buttons via [SlashCommand.buttons].
     */
    @Suppress("KDocUnresolvedReference")
    open fun handleButtonClick(context: ButtonClickContext) {}
}