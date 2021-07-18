package net.greemdev.kcommands

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction

data class SlashCommandContext internal constructor(val event: SlashCommandEvent, val command: SlashCommand) {

    fun name() = event.name
    fun subcommandName() = event.subcommandName
    fun subcommandGroup() = event.subcommandGroup
    fun commandId() = event.commandId
    fun option(name: String) = event.getOption(name)
    fun options(): List<OptionMapping> = event.options

    fun token() = event.token
    fun isDm() = !event.isFromGuild
    fun hook() = event.hook
    fun jda() = event.jda
    fun member() = event.member
    fun user() = event.user
    fun userId() = event.user.id
    fun type() = event.type
    fun messageChannel() = event.messageChannel
    fun privateChannel() = event.privateChannel
    fun textChannel() = event.textChannel
    fun voiceChannel() = event.voiceChannel
    fun acked() = event.isAcknowledged

    fun result(func: SlashCommandResult.() -> Unit) = SlashCommandResult.create(this, func)
    fun result() = SlashCommandResult.getNew(this)


    fun ack(ephemeral: Boolean = false) = event.deferReply(ephemeral)

    @Throws(IllegalArgumentException::class)
    inline fun <reified T> getOptionValue(name: String) =
        getOptionElse<T>(name, null) ?: throw IllegalArgumentException("Option did not have a value.")


    @Suppress("IMPLICIT_CAST_TO_ANY")
    inline fun <reified T> getOptionElse(name: String, default: T?): T? = with(option(name)) {
        return@with if (this == null) default
        else when (T::class) {
            String::class -> asString
            Int::class -> asLong
            Long::class -> asLong
            Boolean::class -> asBoolean
            GuildChannel::class -> asGuildChannel
            Member::class -> asMember
            IMentionable::class -> asMentionable
            MessageChannel::class -> asMessageChannel
            Role::class -> asRole
            User::class -> asUser
            else -> throw IllegalArgumentException("Type is not of a known option type.")
        } as T
    }
}