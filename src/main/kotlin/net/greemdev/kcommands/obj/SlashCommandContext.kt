package net.greemdev.kcommands.obj

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.greemdev.kcommands.SlashCommand
import net.greemdev.kcommands.util.KEmbedBuilder
import kotlin.jvm.Throws

data class SlashCommandContext internal constructor(val event: SlashCommandEvent, val command: SlashCommand) {

    fun name() = event.name
    fun subcommandName() = event.subcommandName
    fun subcommandGroup() = event.subcommandGroup
    fun commandId() = event.commandId
    fun options() = event.options

    fun token() = event.token
    fun isDm() = !event.isFromGuild
    fun hook() = event.hook
    fun jda() = event.jda
    fun member() = event.member
    fun user() = event.user
    fun type() = event.type
    fun messageChannel() = event.messageChannel
    fun privateChannel() = event.privateChannel
    fun textChannel() = event.textChannel
    fun voiceChannel() = event.voiceChannel
    fun acked() = event.isAcknowledged

    fun ack(ephemeral: Boolean = false) = event.deferReply(ephemeral)
    infix fun reply(func: KEmbedBuilder.() -> Unit) = event.replyEmbeds(KEmbedBuilder(func).build())
    fun reply(vararg embeds: MessageEmbed) = event.replyEmbeds(embeds.toList())
    infix fun reply(embed: MessageEmbed) = event.replyEmbeds(embed)
    infix fun reply(content: String) = event.reply(content)
    infix fun reply(message: Message) = event.reply(message)
    fun reply(format: String, vararg args: Any) = event.replyFormat(format, args)
    fun option(name: String) = event.getOption(name)

    @Throws(IllegalArgumentException::class)
    inline fun <reified T> getOptionValue(name: String)
            = getOptionElse<T>(name, null) ?: throw IllegalArgumentException("Option did not have a value.")


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
        } as T?

    }

}