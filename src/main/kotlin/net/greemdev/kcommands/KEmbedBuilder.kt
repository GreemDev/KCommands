package net.greemdev.kcommands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor

fun embed(func: KEmbedBuilder.() -> Unit): MessageEmbed {
    return KEmbedBuilder(func).build()
}

data class KEmbedBuilder internal constructor(private var backing: EmbedBuilder = EmbedBuilder()) {

    companion object {
        infix fun of(builder: EmbedBuilder): KEmbedBuilder = KEmbedBuilder(builder)
        infix fun of(embed: MessageEmbed): KEmbedBuilder = KEmbedBuilder(embed)
        infix fun of(func: KEmbedBuilder.() -> Unit): KEmbedBuilder = KEmbedBuilder(func)
    }

    constructor(from: MessageEmbed) : this(EmbedBuilder(from))

    constructor(initializer: KEmbedBuilder.() -> Unit) : this(KEmbedBuilder().apply(initializer).backing)

    fun reset(): KEmbedBuilder = this.also { backing.clear() }
    fun length(): Int = backing.length()
    inline infix fun fields(func: FieldCreationScope.() -> Unit): KEmbedBuilder = this.also { FieldCreationScope().func() }
    fun field(name: String, body: Any, inline: Boolean): KEmbedBuilder = this.also { backing.addField(name, body.toString(), inline) }
    infix fun description(content: CharSequence?): KEmbedBuilder = this.also { backing.setDescription(content) }
    infix fun appendDescription(content: CharSequence): KEmbedBuilder = this.also { backing.appendDescription(content) }
    infix fun buildDescription(func: StringBuilder.() -> Unit): KEmbedBuilder = this.also { backing.setDescription(buildString(func)) }
    fun author(name: String, url: String? = null, iconUrl: String? = null): KEmbedBuilder = this.also { backing.setAuthor(name, url, iconUrl) }
    fun footer(text: String?, iconUrl: String? = null): KEmbedBuilder = this.also { backing.setFooter(text, iconUrl) }
    infix fun timestamp(temporal: TemporalAccessor): KEmbedBuilder = this.also { backing.setTimestamp(temporal) }
    infix fun color(color: Color?): KEmbedBuilder = this.also { backing.setColor(color) }
    infix fun colorRaw(color: Int): KEmbedBuilder = this.also { backing.setColor(color) }
    infix fun image(url: String?): KEmbedBuilder = this.also { backing.setImage(url) }
    infix fun thumbnail(url: String?): KEmbedBuilder = this.also { backing.setThumbnail(url) }
    fun title(title: String?, url: String? = null): KEmbedBuilder = this.also { backing.setTitle(title, url) }
    fun build(): MessageEmbed = backing.build()

    inner class FieldCreationScope {
        fun normal(name: String, value: Any) = this.also { field(name, value, false) }
        fun inline(name: String, value: Any) = this.also { field(name, value, true) }
        fun blank(inline: Boolean = false) = this.also { backing.addBlankField(inline) }
        fun raw(field: MessageEmbed.Field?) = this.also { backing.addField(field) }
    }
}