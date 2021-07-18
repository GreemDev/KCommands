@file:JvmName("CoreUtil")

package net.greemdev.kcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.OptionType.*
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.dv8tion.jda.internal.JDAImpl

fun SlashCommand.toData(): CommandData {
    return with(CommandData(name.lowercase(), description)) {
        if (options.isNotEmpty()) this.addOptions(*this@toData.options)
        this
    }
}

infix fun JDA.upsertCommand(command: SlashCommand): CommandCreateAction? {
    val data = command.toData()
    return if (command is GuildSlashCommand)
        getGuildById(command.guildId)?.upsertCommand(data)
    else upsertCommand(data)
}

infix fun JDA.applicationCommands(func: ApplicationCommandCreationScope.() -> Unit): CommandListUpdateAction =
    updateCommands().also { CreationScopes.commands(it).func() }

/**
 * Loops over every [SlashCommand] in the set and creates an upsert [CommandCreateAction] to unify application commands; then queues them all.
 */
infix fun JDA.withApplicationCommands(slashCommands: Collection<SlashCommand>) {
    val actions = slashCommands.mapNotNull(this::upsertCommand)

    RestAction.allOf(actions).queue { list ->
        JDAImpl.LOG.info("Registered ${list.size} slash commands: [${
            list.joinToString(", ") { "\"${it.name}\"" }
        }]")
    }
}

infix fun Guild.applicationCommands(func: ApplicationCommandCreationScope.() -> Unit): CommandListUpdateAction =
    updateCommands().also { CreationScopes.commands(it).func() }

fun ReplyAction.ephemeral(ephemeral: Boolean = true): ReplyAction {
    setEphemeral(ephemeral)
    return this
}

fun ButtonClickEvent.parsedId() = ComponentId.new(this.componentId)
fun SelectionMenuEvent.parsedId() = ComponentId.new(this.componentId)

fun Component.parsedId() =
    ComponentId.new(this.id ?: throw IllegalStateException("Cannot use ButtonComponentId with a link Button!"))

fun actionRow(component: Component): ActionRow = ActionRow.of(component)
fun actionRow(components: Collection<Component>): ActionRow = ActionRow.of(components)
fun actionRow(vararg components: Component): ActionRow = actionRow(components.toSet())

fun ReplyAction.actionRow(func: () -> Component): ReplyAction = addActionRow(func())
fun ReplyAction.actionRows(func: () -> Collection<Component>): ReplyAction = allActionRows(*func().toList().toTypedArray())
fun ReplyAction.allActionRows(vararg components: Component): ReplyAction = addActionRow(*components)
fun ReplyAction.buttonsFrom(context: SlashCommandContext): ReplyAction =
    actionRows { context.command.components().buttons(context).toHashSet() }
fun ReplyAction.selectionMenusFrom(context: SlashCommandContext): ReplyAction =
    actionRows { context.command.components().selectionMenus(context).toHashSet() }
fun ReplyAction.allComponentsFrom(context: SlashCommandContext): ReplyAction =
    buttonsFrom(context).selectionMenusFrom(context)


/**
 * Adds all the choices in [pairs] to the current [OptionData], using [Pair.first] as the name and [Pair.second] as the value.
 */
fun OptionData.choices(vararg pairs: Pair<String, String>) {
    pairs.forEach { this.addChoice(it.first, it.second) }
}

/**
 * Adds all the choices in [choices] to the current [OptionData], using the choice [String] as the choice name and value.
 */
fun OptionData.choices(vararg choices: String) {
    choices(*choices.map { it to it }.toTypedArray())
}

/**
 * An excessively high-level wrapper for the basic Discord Component ID system.
 *
 * [name] is the command name;
 *
 * [user] is the user that clicked the button;
 *
 * [action] is the action to perform, useful for commands with multiple buttons, or even paginators;
 *
 * [value] is the value passed, useful for having multiple buttons with differing values for options to choose what people can do.
 * To get the finished value, call [asString] and you'll get the formatted ID.
 *
 *
 * The formatted ID follows a specific format: `name:user:action:value`
 */
data class ComponentId(val sb: StringBuilder = StringBuilder()) {

    fun raw() = sb.toString()

    val sep = SlashCommandClient.get().config().componentIdSeparator

    companion object {
        @JvmStatic
        fun new(): ComponentId = ComponentId()

        @JvmStatic
        fun new(initialValue: CharSequence): ComponentId = ComponentId(
            if (initialValue is StringBuilder) initialValue else StringBuilder(initialValue)
        )

        @JvmStatic
        fun from(command: SlashCommand): ComponentId = new().name(command.name)
    }

    fun name(): String? = tryOrNull { raw().split(sep)[0] }
    fun user(): String? = tryOrNull { raw().split(sep)[1] }
    fun action(): String? = tryOrNull { raw().split(sep)[2] }
    fun value(): String? = tryOrNull { raw().split(sep)[3] }

    fun name(value: Any) = this.apply {
        sb.append("$value$sep")
    }

    fun user(value: Any) = this.apply {
        sb.append("$value$sep")
    }


    fun action(value: Any) = this.apply {
        sb.append("$value$sep")
    }


    fun value(value: Any?) = this.apply {
        sb.append("$value$sep")
    }


    fun asString(): String = sb.toString().trimEnd { it == sep }
}

internal object CreationScopes {
    internal object Headless {
        fun options(options: HashSet<OptionData> = hashSetOf()) = HeadlessApplicationCommandOptionCreationScope(options)
        fun checks(checks: HashSet<SlashCommandCheck> = hashSetOf()) = HeadlessApplicationCommandCheckCreationScope(checks)
        fun buttons(buttons: HashSet<Button> = hashSetOf()) = HeadlessApplicationCommandButtonCreationScope(buttons)
        fun selectionMenus(menus: HashSet<SelectionMenu> = hashSetOf()) = HeadlessApplicationCommandSelectionMenuCreationScope(menus)
    }

    fun commands(action: CommandListUpdateAction) = ApplicationCommandCreationScope(action)
    fun options(data: CommandData) = ApplicationCommandOptionCreationScope(data)
    fun messageComponents(command: SlashCommand) = ApplicationCommandMessageComponentCreationScope(command)
}


fun buildOptions(func: HeadlessApplicationCommandOptionCreationScope.() -> Unit): Array<OptionData> {
    return CreationScopes.Headless.options().apply(func).options.toTypedArray()
}

data class ApplicationCommandMessageComponentCreationScope(private val command: SlashCommand) {
    /**
     * Configure the [Button]s for use when this command's result is being sent;
     * with the value of the [SlashCommandContext] passed as a parameter to utilize event-time data in [ComponentId]s.
     */
    fun buttons(func: HeadlessApplicationCommandButtonCreationScope.(SlashCommandContext) -> Unit) {
        command.components().lazyButtons = func
    }

    /**
     * Configure the [SelectionMenu]s for use when this command's result is being sent; with the value of the [SlashCommandContext] passed as a parameter to utilize event-time data.
     */
    fun selectionMenus(func: HeadlessApplicationCommandSelectionMenuCreationScope.(SlashCommandContext) -> Unit) {
        command.components().lazySelectionMenus = func
    }
}

data class HeadlessApplicationCommandCheckCreationScope(val checks: HashSet<SlashCommandCheck> = hashSetOf()) {

    fun check(predicate: (SlashCommandContext) -> Boolean, initializer: SlashCommandCheck.() -> Unit) {
        checks.add(SlashCommandCheck(predicate).apply(initializer))
    }

    fun check(initializer: SlashCommandCheck.() -> Unit) {
        checks.add(SlashCommandCheck().apply(initializer))
    }

    operator fun plusAssign(other: Pair<String, (SlashCommandContext) -> Boolean>) {
        check(other.first, other.second)
    }

    fun requireUserAdministrator() {
        check(SlashCommandClient.get().config().checkUserNotAdmin) {
            it.member()?.hasPermission(Permission.ADMINISTRATOR) ?: false
        }
    }

    fun requireUserApplicationOwner() {
        check(SlashCommandClient.get().config().checkUserNotOwner) {
            it.userId() in SlashCommandClient.get().applicationOwners
        }
    }

    fun requireUserPermission(vararg perms: Permission) {
        check {
            predicate {
                val failed = hashSetOf<Permission>()
                if (it.member() == null) {
                    reason("User not in a guild.")
                    return@predicate false
                }

                for (perm in perms) {
                    if (!it.member()!!.hasPermission(perm))
                        failed.add(perm)
                }

                if (failed.isNotEmpty()) {
                    reason("User does not have the following permissions: ${markdown(failed.joinToString(", ") { p -> p.getName() }).inlineCode()}")
                    false
                } else true
            }
        }
    }

    fun check(failureReason: String, predicate: (SlashCommandContext) -> Boolean) {
        checks.add(SlashCommandCheck(predicate) {
            reason(failureReason)
        })
    }
}

data class HeadlessApplicationCommandSelectionMenuCreationScope(val menus: HashSet<SelectionMenu> = hashSetOf()) {
    fun menu(id: String, func: SelectionMenu.Builder.() -> Unit) {
        menus.add(SelectionMenu.create(id).apply(func).build())
    }
}

data class HeadlessApplicationCommandButtonCreationScope(val buttons: HashSet<Button> = hashSetOf()) {
    fun create(style: ButtonStyle, idOrUrl: String, label: String, emoji: Emoji? = null) {
        buttons.add(Button.of(style, idOrUrl, label).withEmoji(emoji))
    }

    fun create(style: ButtonStyle, idOrUrl: String, emoji: Emoji) {
        buttons.add(Button.of(style, idOrUrl, emoji))
    }

    fun danger(id: ComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.DANGER, id.asString(), label, emoji)

    fun danger(id: ComponentId, emoji: Emoji) = create(ButtonStyle.DANGER, id.asString(), emoji)

    fun success(id: ComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SUCCESS, id.asString(), label, emoji)

    fun success(id: ComponentId, emoji: Emoji) = create(ButtonStyle.SUCCESS, id.asString(), emoji)


    fun link(url: String, label: String, emoji: Emoji? = null) {
        buttons.add(Button.link(url, label).withEmoji(emoji))
    }

    fun link(url: String, emoji: Emoji) {
        buttons.add(Button.link(url, emoji))
    }

    fun primary(id: ComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.PRIMARY, id.asString(), label, emoji)

    fun primary(id: ComponentId, emoji: Emoji) = create(ButtonStyle.PRIMARY, id.asString(), emoji)

    fun secondary(id: ComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SECONDARY, id.asString(), label, emoji)

    fun secondary(id: ComponentId, emoji: Emoji) = create(ButtonStyle.SECONDARY, id.asString(), emoji)

}

data class HeadlessApplicationCommandOptionCreationScope(val options: HashSet<OptionData> = hashSetOf()) {
    private fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit) {
        options.add(OptionData(type, name, description, false).apply(func))
    }

    fun optionalString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(STRING, name, description, func)

    fun optionalBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(BOOLEAN, name, description, func)

    fun optionalUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(USER, name, description, func)

    fun optionalChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(CHANNEL, name, description, func)

    fun optionalInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(INTEGER, name, description, func)

    fun optionalMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(MENTIONABLE, name, description, func)

    fun optionalRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(ROLE, name, description, func)

    private fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit) {
        options.add(OptionData(type, name, description, true).apply(func))
    }

    fun requiredString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(STRING, name, description, func)

    fun requiredBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(BOOLEAN, name, description, func)

    fun requiredUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(USER, name, description, func)

    fun requiredChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(CHANNEL, name, description, func)

    fun requiredInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(INTEGER, name, description, func)

    fun requiredMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(MENTIONABLE, name, description, func)

    fun requiredRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(ROLE, name, description, func)
}


data class ApplicationCommandCreationScope(private val action: CommandListUpdateAction) {
    fun command(name: String, description: String, func: ApplicationCommandOptionCreationScope.() -> Unit) {
        val commandData = CommandData(name, description)
        func(CreationScopes.options(commandData))
        action.addCommands(commandData)
    }

    fun command(name: String, description: String) {
        action.addCommands(CommandData(name, description))
    }
}

data class ApplicationCommandOptionCreationScope(private val data: CommandData) {

    private fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit) {
        data.addOptions(OptionData(type, name, description, false).apply(func))
    }

    fun optionalString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(STRING, name, description, func)

    fun optionalBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(BOOLEAN, name, description, func)

    fun optionalUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(USER, name, description, func)

    fun optionalChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(CHANNEL, name, description, func)

    fun optionalInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(INTEGER, name, description, func)

    fun optionalMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(MENTIONABLE, name, description, func)

    fun optionalRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(ROLE, name, description, func)

    private fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit) {
        data.addOptions(OptionData(type, name, description, true).apply(func))
    }

    fun requiredString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(STRING, name, description, func)

    fun requiredBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(BOOLEAN, name, description, func)

    fun requiredUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(USER, name, description, func)

    fun requiredChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(CHANNEL, name, description, func)

    fun requiredInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(INTEGER, name, description, func)

    fun requiredMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(MENTIONABLE, name, description, func)

    fun requiredRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(ROLE, name, description, func)
}

fun <V> tryOrNull(func: () -> V): V? = try {
    func()
} catch (t: Throwable) {
    null
}

fun markdown(value: String) = Markdown(value)

data class Markdown internal constructor(var value: String) {
    private fun String.surround(with: String) = "$with$this$with"

    fun bold() = value.surround("**")
    fun italicize() = value.surround("*")
    fun spoiler() = value.surround("||")
    fun underline() = value.surround("__")
    fun inlineCode() = value.surround("`")
    fun blockCode(lang: String = "") = "${if (lang.isNotEmpty()) "$lang\n" else ""}${value}".surround("```")
}