package net.greemdev.kcommands

import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.greemdev.kcommands.*

/**
 * The base class for all Slash Commands. This provides a high-level API for creating and executing Slash Commands;
 * along with components, specifically buttons.
 */
@Suppress("KDocUnresolvedReference")
abstract class SlashCommand(val name: String, val description: String) {

    var options: Array<OptionData> = arrayOf()
    var checks: Array<SlashCommandCheck> = arrayOf()

    private val components = Components()

    inner class Components {

        var lazyButtons: HeadlessApplicationCommandButtonCreationScope.(SlashCommandContext) -> Unit = { }
        var lazySelectionMenus: HeadlessApplicationCommandSelectionMenuCreationScope.(SlashCommandContext) -> Unit = { }

        fun buttons(context: SlashCommandContext): HashSet<Button> =
            CreationScopes.Headless.buttons().apply { lazyButtons(context) }.buttons
        fun selectionMenus(context: SlashCommandContext): HashSet<SelectionMenu> =
            CreationScopes.Headless.selectionMenus().apply { lazySelectionMenus(context) }.menus
    }

    /**
     * Iterates through [checks] and returns all of the [SlashCommandCheck]s that fail in a [HashSet].
     * @return The checks that have failed; if the [HashSet] is empty then the command should be executed.
     */
    fun runChecks(ctx: SlashCommandContext): Set<SlashCommandCheck> {
        return checks.filter { !it.check(ctx) }.toHashSet()
    }

    fun components() = components

    fun components(func: ApplicationCommandMessageComponentCreationScope.() -> Unit) {
        CreationScopes.messageComponents(this).apply(func)
    }

    /**
     * Configure the options for use with this command, for use on initialization to upsert commands.
     */
    fun options(func: HeadlessApplicationCommandOptionCreationScope.() -> Unit) {
        options = buildOptions(func)
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
     * Creates a new [ComponentId] with `commandName`, its first piece, set as the current command's name.
     */
    fun newComponentId() = ComponentId.from(this)

    /**
     * Creates a new [ComponentId] from [newComponentId] and then applies [func] to it allowing for receiver creation of [ComponentId]s.
     */
    fun componentId(func: ComponentId.() -> Unit) = newComponentId().apply(func)

    /**
     * The function called when this command has been invoked from Discord.
     * You need to ack the request within 3 seconds via [SlashCommandContext.ack] or [SlashCommandContext.reply].
     * Because this function is [abstract], when extending this class you need to override this.
     */
    abstract fun handleSlashCommand(context: SlashCommandContext): SlashCommandResult

    /**
     * The function called when buttons on messages made from this command have been clicked.
     * (This means the component ID begins with the name provided when creating this class.)
     * You need to ack the request within 3 seconds via [ButtonClickContext.ack], [ButtonClickContext.reply], [ButtonClickContext.editMessage]-type functions.
     * Because this function is [open], when extending this class you do not need to override this; unless you want to utilize Buttons via [SlashCommand.buttons].
     */
    open fun handleButtonClick(context: ButtonClickContext) {}

    /**
     * The function called when [SelectionMenu]s on messages made from this command have been used. (This means the component ID begins with the name provided when creating this class.)
     * You need to ack the request within 3 seconds via [SelectionMenuContext.ack], [SelectionMenuContext.reply], [SelectionMenuContext.editMessage]-type functions. Because this function is [open],
     * when extending this class you do not need to override this; unless you want to utilize [SelectionMenu]s via [SlashCommand.selectionMenus].
     */
    open fun handleSelectionMenu(context: SelectionMenuContext) {}
}