@file:JvmName("InteractionUtil")

package net.greemdev.kcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.OptionType.*
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.dv8tion.jda.internal.JDAImpl

infix fun JDA.applicationCommands(func: ApplicationCommandCreationScope.() -> Unit): CommandListUpdateAction =
    updateCommands().also { CreationScopes.commands(it).func() }

/**
 * Loops over every [SlashCommand] in the set and creates an upsert [CommandCreateAction] to unify application commands; then queues them all.
 */
infix fun JDA.withApplicationCommands(slashCommands: Collection<SlashCommand>) {
    val actions = slashCommands.map { command ->
        val data = CommandData(command.name.lowercase(), command.description)
        if (command.options.isNotEmpty())
            data.addOptions(*command.options)
        if (command is GuildSlashCommand)
            getGuildById(command.guildId)?.upsertCommand(data)
        else
            upsertCommand(data)
    }

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

fun ButtonClickEvent.parsedId() = ButtonComponentId.new(this.componentId)

fun Component.parsedId() =
    ButtonComponentId.new(this.id ?: throw IllegalStateException("Cannot use ButtonComponentId with a link Button!"))

fun ReplyAction.actionRow(func: () -> Component): ReplyAction = addActionRow(func())
fun ReplyAction.actionRows(func: () -> Collection<Component>): ReplyAction = allActionRows(*func().toTypedArray())
fun ReplyAction.allActionRows(vararg components: Component): ReplyAction = addActionRow(*components)
fun ReplyAction.actionRowsFrom(context: SlashCommandContext): ReplyAction =
    addActionRow(*context.command.buttons(context))

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
data class ButtonComponentId(val sb: StringBuilder = StringBuilder()) {

    fun raw() = sb.toString()

    val sep = SlashCommandClient.get().config.componentIdSeparator

    companion object {
        @JvmStatic
        fun new(): ButtonComponentId = ButtonComponentId()

        @JvmStatic
        fun new(initialValue: CharSequence): ButtonComponentId = ButtonComponentId(
            if (initialValue is StringBuilder) initialValue else StringBuilder(initialValue)
        )

        @JvmStatic
        fun from(command: SlashCommand): ButtonComponentId = new().name(command.name)
    }

    fun name(): String? = executeElseNull { raw().split(sep)[0] }
    fun user(): String? = executeElseNull { raw().split(sep)[1] }
    fun action(): String? = executeElseNull { raw().split(sep)[2] }
    fun value(): String? = executeElseNull { raw().split(sep)[3] }

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
        fun checks(checks: HashSet<SlashCommandCheck> = hashSetOf()) =
            HeadlessApplicationCommandCheckCreationScope(checks)

        fun buttons(buttons: HashSet<Button> = hashSetOf()) = HeadlessApplicationCommandButtonCreationScope(buttons)
    }

    fun commands(action: CommandListUpdateAction) = ApplicationCommandCreationScope(action)
    fun options(data: CommandData) = ApplicationCommandOptionCreationScope(data)
}


fun buildOptions(func: HeadlessApplicationCommandOptionCreationScope.() -> Unit): Array<OptionData> {
    return CreationScopes.Headless.options().apply(func).options.toTypedArray()
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
        check(SlashCommandClient.get().config.checkUserNotAdmin) {
            it.member()?.hasPermission(Permission.ADMINISTRATOR) ?: false
        }
    }

    fun requireUserApplicationOwner() {
        check(SlashCommandClient.get().config.checkUserNotOwner) {
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

data class HeadlessApplicationCommandButtonCreationScope(val buttons: HashSet<Button> = hashSetOf()) {
    fun create(style: ButtonStyle, idOrUrl: String, label: String, emoji: Emoji? = null) {
        buttons.add(Button.of(style, idOrUrl, label).withEmoji(emoji))
    }

    fun create(style: ButtonStyle, idOrUrl: String, emoji: Emoji) {
        buttons.add(Button.of(style, idOrUrl, emoji))
    }

    fun danger(id: ButtonComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.DANGER, id.asString(), label, emoji)

    fun danger(id: ButtonComponentId, emoji: Emoji) = create(ButtonStyle.DANGER, id.asString(), emoji)

    fun success(id: ButtonComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SUCCESS, id.asString(), label, emoji)

    fun success(id: ButtonComponentId, emoji: Emoji) = create(ButtonStyle.SUCCESS, id.asString(), emoji)


    fun link(url: String, label: String, emoji: Emoji? = null) {
        buttons.add(Button.link(url, label).withEmoji(emoji))
    }

    fun link(url: String, emoji: Emoji) {
        buttons.add(Button.link(url, emoji))
    }

    fun primary(id: ButtonComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.PRIMARY, id.asString(), label, emoji)

    fun primary(id: ButtonComponentId, emoji: Emoji) = create(ButtonStyle.PRIMARY, id.asString(), emoji)

    fun secondary(id: ButtonComponentId, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SECONDARY, id.asString(), label, emoji)

    fun secondary(id: ButtonComponentId, emoji: Emoji) = create(ButtonStyle.SECONDARY, id.asString(), emoji)

}

data class HeadlessApplicationCommandOptionCreationScope(val options: HashSet<OptionData> = hashSetOf()) {
    private fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
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

    private fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
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

    private fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
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

    private fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
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