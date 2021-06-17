@file:JvmName("InteractionUtil")

package net.greemdev.kcommands.ext

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
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
import net.greemdev.kcommands.GuildSlashCommand
import net.greemdev.kcommands.SlashCommand
import net.greemdev.kcommands.obj.SlashCommandCheck
import net.greemdev.kcommands.obj.SlashCommandContext
import net.greemdev.kcommands.util.executeElseNull

infix fun JDA.applicationCommands(func: ApplicationCommandCreationScope.() -> Unit): CommandListUpdateAction =
    updateCommands().also { CreationScopes.commands(it).func() }

/**
 * Loops over every [SlashCommand] in the set and creates an upsert [CommandCreateAction] to unify application commands; then queues them all.
 */
infix fun JDA.withApplicationCommands(slashCommands: Set<SlashCommand>) {
    val actions = slashCommands.map { command ->
        val data = CommandData(command.name.toLowerCase(), command.description)
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

fun ButtonClickEvent.parsedId() = ComponentIdBuilder.from(this.componentId)

fun Component.parsedId() =
    ComponentIdBuilder.from(this.id ?: throw IllegalStateException("Cannot use ComponentIdBuilder with a link Button!"))

fun ReplyAction.actionRow(func: () -> Component): ReplyAction = addActionRow(func())
fun ReplyAction.actionRows(func: () -> Collection<Component>): ReplyAction = allActionRows(*func().toTypedArray())
fun ReplyAction.allActionRows(vararg components: Component): ReplyAction = addActionRow(*components)
fun ReplyAction.actionRowsFrom(context: SlashCommandContext): ReplyAction =
    addActionRow(*context.command.buttons(context))

data class ComponentIdBuilder(val sb: StringBuilder = StringBuilder()) {

    fun raw() = sb.toString()

    companion object {
        @JvmStatic
        fun new(): ComponentIdBuilder = ComponentIdBuilder()
        @JvmStatic
        fun from(componentId: String): ComponentIdBuilder = ComponentIdBuilder(StringBuilder(componentId))
        @JvmStatic
        fun from(command: SlashCommand): ComponentIdBuilder = ComponentIdBuilder().name(command.name)
    }

    fun name(): String? = executeElseNull { raw().split(':')[0] }
    fun user(): String? = executeElseNull { raw().split(':')[1] }
    fun action(): String? = executeElseNull { raw().split(':')[2] }
    fun value(): String? = executeElseNull { raw().split(':')[3] }

    fun name(value: String) = this.apply {
        sb.append("$value:")
    }

    fun user(value: String) = this.apply {
        sb.append("$value:")
    }


    fun action(value: String) = this.apply {
        sb.append("$value:")
    }


    fun value(value: Any?) = this.apply {
        sb.append("$value:")
    }


    fun build(): String = sb.toString().trimEnd { it == ':' }
}

object CreationScopes {
    fun headlessOptions(options: HashSet<OptionData> = hashSetOf()) =
        HeadlessApplicationCommandOptionCreationScope(options)

    fun headlessChecks(checks: HashSet<SlashCommandCheck> = hashSetOf()) =
        HeadlessApplicationCommandCheckCreationScope(checks)

    fun headlessButtons(buttons: HashSet<Button> = hashSetOf()) = HeadlessApplicationCommandButtonCreationScope(buttons)
    fun commands(action: CommandListUpdateAction) = ApplicationCommandCreationScope(action)
    fun options(data: CommandData) = ApplicationCommandOptionCreationScope(data)

}


fun buildOptions(func: HeadlessApplicationCommandOptionCreationScope.() -> Unit): Array<OptionData> {
    return CreationScopes.headlessOptions().apply(func).options.toTypedArray()
}

data class HeadlessApplicationCommandCheckCreationScope(val checks: HashSet<SlashCommandCheck> = hashSetOf()) {
    fun check(predicate: (SlashCommandContext) -> Boolean, initializer: SlashCommandCheck.() -> Unit) {
        checks.add(SlashCommandCheck(predicate).apply(initializer))
    }

    fun check(initializer: SlashCommandCheck.() -> Unit) {
        checks.add(SlashCommandCheck{true}.apply(initializer))
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

    fun danger(id: ComponentIdBuilder, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.DANGER, id.build(), label, emoji)

    fun danger(id: ComponentIdBuilder, emoji: Emoji) = create(ButtonStyle.DANGER, id.build(), emoji)

    fun success(id: ComponentIdBuilder, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SUCCESS, id.build(), label, emoji)

    fun success(id: ComponentIdBuilder, emoji: Emoji) = create(ButtonStyle.SUCCESS, id.build(), emoji)


    fun link(url: String, label: String, emoji: Emoji? = null) {
        buttons.add(Button.link(url, label).withEmoji(emoji))
    }

    fun link(url: String, emoji: Emoji) {
        buttons.add(Button.link(url, emoji))
    }

    fun primary(id: ComponentIdBuilder, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.PRIMARY, id.build(), label, emoji)

    fun primary(id: ComponentIdBuilder, emoji: Emoji) = create(ButtonStyle.PRIMARY, id.build(), emoji)

    fun secondary(id: ComponentIdBuilder, label: String, emoji: Emoji? = null) =
        create(ButtonStyle.SECONDARY, id.build(), label, emoji)

    fun secondary(id: ComponentIdBuilder, emoji: Emoji) = create(ButtonStyle.SECONDARY, id.build(), emoji)


}

data class HeadlessApplicationCommandOptionCreationScope(val options: HashSet<OptionData>) {
    fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
        options.add(OptionData(type, name, description, false).apply(func))
    }

    fun optionalString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.STRING, name, description, func)

    fun optionalBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.BOOLEAN, name, description, func)

    fun optionalUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.USER, name, description, func)

    fun optionalChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.CHANNEL, name, description, func)

    fun optionalInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.INTEGER, name, description, func)

    fun optionalMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.MENTIONABLE, name, description, func)

    fun optionalRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.ROLE, name, description, func)
    //fun optionalSubcommand(name: String, description: String) = optional(OptionType.SUB_COMMAND, name, description)
    //fun optionalSubcommandGroup(name: String, description: String) = optional(OptionType.SUB_COMMAND_GROUP, name, description)

    fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
        options.add(OptionData(type, name, description, true).apply(func))
    }

    fun requiredString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.STRING, name, description, func)

    fun requiredBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.BOOLEAN, name, description, func)

    fun requiredUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.USER, name, description, func)

    fun requiredChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.CHANNEL, name, description, func)

    fun requiredInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.INTEGER, name, description, func)

    fun requiredMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.MENTIONABLE, name, description, func)

    fun requiredRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.ROLE, name, description, func)
    //fun requiredSubcommand(name: String, description: String) = required(OptionType.SUB_COMMAND, name, description)
    //fun requiredSubcommandGroup(name: String, description: String) = required(OptionType.SUB_COMMAND_GROUP, name, description)
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

    fun optional(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
        data.addOptions(OptionData(type, name, description, false).apply(func))
    }

    fun optionalString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.STRING, name, description, func)

    fun optionalBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.BOOLEAN, name, description, func)

    fun optionalUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.USER, name, description, func)

    fun optionalChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.CHANNEL, name, description, func)

    fun optionalInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.INTEGER, name, description, func)

    fun optionalMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.MENTIONABLE, name, description, func)

    fun optionalRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        optional(OptionType.ROLE, name, description, func)

    fun required(type: OptionType, name: String, description: String, func: OptionData.() -> Unit = {}) {
        data.addOptions(OptionData(type, name, description, true).apply(func))
    }

    fun requiredString(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.STRING, name, description, func)

    fun requiredBoolean(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.BOOLEAN, name, description, func)

    fun requiredUser(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.USER, name, description, func)

    fun requiredChannel(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.CHANNEL, name, description, func)

    fun requiredInt(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.INTEGER, name, description, func)

    fun requiredMentionable(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.MENTIONABLE, name, description, func)

    fun requiredRole(name: String, description: String, func: OptionData.() -> Unit = {}) =
        required(OptionType.ROLE, name, description, func)
}