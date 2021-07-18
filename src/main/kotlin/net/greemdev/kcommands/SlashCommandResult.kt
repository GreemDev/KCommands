package net.greemdev.kcommands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
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
    private var actionRows: HashSet<ActionRow> = hashSetOf()

    infix fun withRawModification(func: ReplyAction.() -> Unit): SlashCommandResult = this.also {
        this.actionModifiers.add(func)
    }


    internal fun buildAsRestAction(): ReplyAction {
        val action = if (content != null)
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

        return action.ephemeral(ephemeral).apply { actionModifiers.forEach { it(this) } }
    }

    /**
     * += operator overload for [withRawModification].
     * @sample "this += { ephemeral() }"
     */
    operator fun plusAssign(other: ReplyAction.() -> Unit) {
        actionModifiers.add(other)
    }

    fun withButtons(): SlashCommandResult = this.also {
        this.actionRows.add(ActionRow.of(ctx.command.components().buttons(ctx)))
    }


    fun withButtons(vararg buttons: Button): SlashCommandResult = this.also {
        this.actionRows.add(ActionRow.of(buttons.toHashSet()))
    }


    fun withSelectionMenus(): SlashCommandResult = this.also {
        this.actionRows.add(ActionRow.of(ctx.command.components().selectionMenus(ctx)))
    }


    fun withAllCommandComponents(): SlashCommandResult = this.withButtons().withSelectionMenus()

    fun withSelectionMenus(vararg menus: SelectionMenu): SlashCommandResult = this.also {
        this.actionRows.add(ActionRow.of(menus.toHashSet()))
    }


    fun ephemeral(): SlashCommandResult = this.also {
        ephemeral = true
    }

    fun notEphemeral(): SlashCommandResult = this.also {
        ephemeral = false
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