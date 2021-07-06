package net.greemdev.kcommands.obj

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.greemdev.kcommands.SlashCommand
import net.greemdev.kcommands.util.KEmbedBuilder

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
    fun userId() = event.user.id
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

    /**
     * Receiver function allowing the ability to reply to a command in a receiver with custom [ReplyAction] modification functions; automatically queueing the RestAction.
     * @sample replyAsync
     */
    infix fun replyAsync(func: ReplyActionScope.() -> Unit) {
        ReplyActionScope().apply(func).restAction().queue()
    }

    /**
     * Does exactly what [replyAsync] does; except automatically adds the buttons produced from invoking [SlashCommand.buttons].
     */
    infix fun replyWithButtonsAsync(func: ReplyActionScope.() -> Unit) {
        ReplyActionScope().apply(func).apply {
            buttons(this@SlashCommandContext)
        }.restAction().queue()
    }

    fun reply(format: String, vararg args: Any) = event.replyFormat(format, args)
    fun option(name: String) = event.getOption(name)

    inner class ReplyActionScope {

        private var content: String? = null
        private var embeds: HashSet<MessageEmbed> = hashSetOf()
        private var message: Message? = null
        private var format: Pair<String, Array<out Any>>? = null
        private var buttons: HashSet<Button> = hashSetOf()
        private var actionModifier: ReplyAction.() -> Unit = { }

        fun modifier(func: ReplyAction.() -> Unit) {
            this.actionModifier = func
        }

        fun buttons(ctx: SlashCommandContext) {
            buttons.addAll(ctx.command.buttons(ctx))
        }

        fun content(content: String) {
            this.content = content
        }

        fun format(format: String, vararg args: Any) {
            this.format = format to args
        }

        fun embed(func: KEmbedBuilder.() -> Unit) {
            this.embeds.add(KEmbedBuilder(func).build())
        }

        fun embed(embed: MessageEmbed) {
            this.embeds.add(embed)
        }

        fun embeds(vararg embeds: MessageEmbed) {
            this.embeds.addAll(embeds.toList())
        }

        fun restAction(): ReplyAction {
            val action = if (content != null)
                event.reply(content!!)
            else if (embeds.isNotEmpty())
                event.replyEmbeds(embeds)
            else if (message != null)
                event.reply(message!!)
            else if (format != null)
                event.replyFormat(format!!.first, format!!.second)
            else throw IllegalStateException("Cannot form a ReplyAction with no data.")
            if (buttons.isNotEmpty())
                action.addActionRow(*buttons.toTypedArray())
            return action.apply(actionModifier)
        }
    }

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