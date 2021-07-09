package net.greemdev.kcommands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor

fun embed(func: KEmbedBuilder.() -> Unit): MessageEmbed {
    return KEmbedBuilder(func).build()
}

data class KEmbedBuilder(private var backing: EmbedBuilder = EmbedBuilder()) {

    companion object {
        infix fun of(builder: EmbedBuilder): KEmbedBuilder = KEmbedBuilder(builder)
        infix fun of(embed: MessageEmbed): KEmbedBuilder = KEmbedBuilder(embed)
        infix fun of(func: KEmbedBuilder.() -> Unit): KEmbedBuilder = KEmbedBuilder().apply(func)
    }

    constructor(from: MessageEmbed) : this(EmbedBuilder(from))

    constructor(initializer: KEmbedBuilder.() -> Unit) : this(EmbedBuilder(KEmbedBuilder().apply(initializer).build()))

    fun reset(): KEmbedBuilder {
        return this.also { backing.clear() }
    }

    fun length(): Int = backing.length()

    inline fun fields(func: FieldCreationScope.() -> Unit): KEmbedBuilder {
        return this.also { FieldCreationScope().func() }
    }

    fun field(name: String, body: Any): KEmbedBuilder {
        return this.also { backing.addField(name, body.toString(), false) }
    }

    fun fieldInline(name: String, body: Any): KEmbedBuilder {
        return this.also { backing.addField(name, body.toString(), true) }
    }

    infix fun description(content: CharSequence?): KEmbedBuilder {
        return this.also { backing.setDescription(content) }
    }

    infix fun appendDescription(content: CharSequence): KEmbedBuilder {
        return this.also { backing.appendDescription(content) }
    }

    infix fun buildDescription(func: StringBuilder.() -> Unit): KEmbedBuilder {
        return this.also { backing.setDescription(StringBuilder().apply(func)) }
    }

    fun author(name: String, url: String? = null, iconUrl: String? = null): KEmbedBuilder {
        return this.also { backing.setAuthor(name, url, iconUrl) }
    }

    fun footer(text: String?, iconUrl: String? = null): KEmbedBuilder {
        return this.also { backing.setFooter(text, iconUrl) }
    }

    infix fun timestamp(temporal: TemporalAccessor): KEmbedBuilder {
        return this.also { backing.setTimestamp(temporal) }
    }

    infix fun color(color: Color?): KEmbedBuilder {
        return this.also { backing.setColor(color) }
    }

    infix fun colorRaw(color: Int): KEmbedBuilder {
        return this.also { backing.setColor(color) }
    }

    infix fun image(url: String?): KEmbedBuilder {
        return this.also { backing.setImage(url) }
    }

    infix fun thumbnail(url: String?): KEmbedBuilder {
        return this.also { backing.setThumbnail(url) }
    }

    fun title(title: String?, url: String? = null): KEmbedBuilder {
        return this.also { backing.setTitle(title, url) }
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