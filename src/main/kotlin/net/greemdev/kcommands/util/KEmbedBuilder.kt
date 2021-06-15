package net.greemdev.kcommands.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import java.awt.Color
import java.time.Instant
import java.time.temporal.TemporalAccessor

fun embed(func: KEmbedBuilder.() -> Unit): MessageEmbed {
    return KEmbedBuilder(func).build()
}

data class KEmbedBuilder(private var backing: EmbedBuilder) {

    companion object {
        infix fun of(builder: EmbedBuilder): KEmbedBuilder = KEmbedBuilder(builder)
        infix fun of(embed: MessageEmbed): KEmbedBuilder = KEmbedBuilder(embed)
        infix fun of(func: KEmbedBuilder.() -> Unit): KEmbedBuilder = KEmbedBuilder().apply(func)
    }
    constructor() : this(EmbedBuilder())

    constructor(from: MessageEmbed) : this(EmbedBuilder(from))

    constructor(initializer: KEmbedBuilder.() -> Unit) : this(EmbedBuilder(KEmbedBuilder().apply(initializer).build()))

    fun reset(): KEmbedBuilder {
        return this.also { backing.clear() }
    }

    fun length(): Int = backing.length()

    inline fun fields(func: FieldCreationScope.() -> Unit): KEmbedBuilder {
        FieldCreationScope().func()
        return this
    }

    fun field(name: String, body: Any): KEmbedBuilder {
        backing.addField(name, body.toString(), false)
        return this
    }

    fun fieldInline(name: String, body: Any): KEmbedBuilder {
        backing.addField(name, body.toString(), true)
        return this
    }

    infix fun description(content: CharSequence?): KEmbedBuilder {
        backing.setDescription(content)
        return this
    }

    infix fun appendDescription(content: CharSequence): KEmbedBuilder {
        backing.appendDescription(content)
        return this
    }

    infix fun buildDescription(func: StringBuilder.() -> Unit): KEmbedBuilder {
        backing.setDescription(StringBuilder().apply(func))
        return this
    }

    fun author(name: String, url: String? = null, iconUrl: String? = null): KEmbedBuilder {
        backing.setAuthor(name, url, iconUrl)
        return this
    }

    fun footer(text: String?, iconUrl: String? = null): KEmbedBuilder {
        backing.setFooter(text, iconUrl)
        return this
    }

    infix fun timestamp(temporal: TemporalAccessor): KEmbedBuilder {
        backing.setTimestamp(temporal)
        return this
    }

    infix fun color(color: Color?): KEmbedBuilder {
        backing.setColor(color)
        return this
    }

    infix fun colorRaw(color: Int): KEmbedBuilder {
        backing.setColor(color)
        return this
    }

    infix fun image(url: String?): KEmbedBuilder {
        backing.setImage(url)
        return this
    }

    infix fun thumbnail(url: String?): KEmbedBuilder {
        backing.setThumbnail(url)
        return this
    }

    fun title(title: String?, url: String? = null): KEmbedBuilder {
        backing.setTitle(title, url)
        return this
    }

    fun build(): MessageEmbed = backing.build()

    inner class FieldCreationScope {
        fun normal(name: String, value: Any) {
            field(name, value)
        }

        fun inline(name: String, value: Any) {
            fieldInline(name, value)
        }

        fun blank(inline: Boolean = false) {
            backing.addBlankField(inline)
        }

        fun raw(field: MessageEmbed.Field?) {
            backing.addField(field)
        }
    }
}