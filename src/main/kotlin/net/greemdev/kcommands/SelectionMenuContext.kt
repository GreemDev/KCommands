package net.greemdev.kcommands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.interactions.components.ComponentLayout
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import java.lang.IllegalStateException

data class SelectionMenuContext internal constructor(val event: SelectionMenuEvent, val command: SlashCommand) {

    fun ack() = event.deferEdit()

    fun menu() = event.selectionMenu
    fun menuId() = event.parsedId()
    fun selectedMenuOptions(): MutableList<SelectOption> = event.selectedOptions ?: throw IllegalStateException("Cannot access selected Menu options on an ephemeral message.")
    fun componentType() = event.componentType
    fun interaction() = event.interaction
    fun message() = event.message
    fun component() = event.component
    fun editMessageEmbeds(vararg embeds: MessageEmbed) = event.editMessageEmbeds(embeds.toList())
    fun editComponents(vararg layouts: ComponentLayout) = event.editComponents(layouts.toList())
    fun editMessage(content: String) = event.editMessage(content)
    fun editMessage(message: Message) = event.editMessage(message)
    fun editMessageFormat(format: String, vararg args: Any) = event.editMessageFormat(format, args)


    fun token() = event.token
    fun isDm() = !event.isFromGuild
    fun hook() = event.hook
    fun jda() = event.jda
    fun member() = event.member
    fun user() = event.user
    fun type() = event.type
    fun channel() = event.channel
    fun acked() = event.isAcknowledged

    infix fun reply(func: KEmbedBuilder.() -> Unit) = event.replyEmbeds(KEmbedBuilder(func).build())
    fun reply(vararg embeds: MessageEmbed) = event.replyEmbeds(embeds.toList())
    infix fun reply(embed: MessageEmbed) = event.replyEmbeds(embed)
    infix fun reply(content: String) = event.reply(content)
    infix fun reply(message: Message) = event.reply(message)
    fun reply(format: String, vararg args: Any) = event.replyFormat(format, args)

}