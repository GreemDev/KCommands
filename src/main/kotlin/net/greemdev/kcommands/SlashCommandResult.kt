package net.greemdev.kcommands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction

data class SlashCommandResult(val ctx: SlashCommandContext) {

    companion object {
        @JvmStatic
        fun getNew(ctx: SlashCommandContext) = SlashCommandResult(ctx)
        @JvmStatic
        fun create(ctx: SlashCommandContext, func: SlashCommandResult.() -> Unit) = getNew(ctx).apply(func)
    }

    private var ephemeral: Boolean = false
    private var content: String? = null
    private var embeds: HashSet<MessageEmbed> = hashSetOf()
    private var message: Message? = null
    private var format: Pair<String, Array<out Any>>? = null
    private var actionModifiers: HashSet<ReplyAction.() -> Unit> = hashSetOf()
    var hookCallback: (InteractionHook) -> Unit = { }
    private var onlyAck: Boolean = false
    private var actionRows: HashSet<ActionRow> = hashSetOf()

    infix fun withRawModification(func: ReplyAction.() -> Unit): SlashCommandResult = this.also {
        this.actionModifiers.add(func)
    }

    infix fun withCallback(func: (InteractionHook) -> Unit) = this.also {
        hookCallback = func
    }

    fun ack(callback: (InteractionHook) -> Unit) = this.also {
        hookCallback = callback
        onlyAck = true
    }


    internal fun buildAsRestAction(): ReplyAction {
        val action = if (onlyAck)
            ctx.event.deferReply(ephemeral)
        else if (content != null)
            ctx.event.reply(content!!)
        else if (embeds.isNotEmpty())
            ctx.event.replyEmbeds(embeds)
        else if (message != null)
            ctx.event.reply(message!!)
        else if (format != null)
            ctx.event.replyFormat(format!!.first, format!!.second)
        else throw IllegalStateException("Cannot form a ReplyAction with no data.")


        if (actionRows.isNotEmpty())
            action.addActionRows(actionRows)

        return if (onlyAck) action
        else action.ephemeral(ephemeral).apply { actionModifiers.forEach { it(this) } }
    }

    /**
     * += operator overload for [withRawModification].
     * @sample "this += { ephemeral() }"
     */
    operator fun plusAssign(other: ReplyAction.() -> Unit) {
        actionModifiers.add(other)
    }

    fun withButtons(): SlashCommandResult = this.also {
        this.actionRows.add(actionRow(ctx.command.components().buttons(ctx)))
    }


    fun withButtons(vararg buttons: Button): SlashCommandResult = this.also {
        this.actionRows.add(actionRow(*buttons))
    }


    fun withSelectionMenus(): SlashCommandResult = this.also {
        this.actionRows.add(actionRow(ctx.command.components().selectionMenus(ctx)))
    }


    fun withAllCommandComponents(): SlashCommandResult = this.withButtons().withSelectionMenus()

    fun withSelectionMenus(vararg menus: SelectionMenu): SlashCommandResult = this.also {
        this.actionRows.add(actionRow(*menus))
    }

    /**
     * Makes the command's resulting message [ephemeral], which makes the message show only to the command's invoker.
     *
     * Usable with [ack].
     *
     * If you were going to pass [false] into this function; you can just ignore this function as false is the default.
     * This function only has use when you want the message to be ephemeral, or if you have changing
     */
    fun withEphemeral(value: Boolean = true): SlashCommandResult = this.also {
        ephemeral = value
    }


    infix fun withContent(content: String): SlashCommandResult = this.also {
        this.content = content
    }


    fun withContentFormat(format: String, vararg args: Any): SlashCommandResult = this.also {
        this.format = format to args
    }


    infix fun withEmbed(func: KEmbedBuilder.() -> Unit): SlashCommandResult = this.also {
        this.embeds.add(KEmbedBuilder(func).build())
    }


    infix fun withEmbed(embed: MessageEmbed): SlashCommandResult = this.also {
        this.embeds.add(embed)
    }


    fun withEmbeds(vararg embeds: MessageEmbed): SlashCommandResult = this.also {
        this.embeds.addAll(embeds.toList())
    }


}