package net.greemdev.kcommands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Invite

/**
 * Represents a regular [SlashCommand] tied to a specific guild represented by [guildId].
 * These can be passed to the [SlashCommandClientConfig] just like regular commands, and on initialization the client will upsert the command to the specific guild;
 * and will still handle it just like any other registered [SlashCommand].
 */
abstract class GuildSlashCommand(val guildId: String, name: String, description: String) : SlashCommand(name, description) {
    fun usableIn(guild: Guild?): Boolean = usableIn(guild?.id)
    fun usableIn(id: String?): Boolean = guildId == id
    fun usableIn(guild: Invite.Guild?): Boolean = usableIn(guild?.id)
}
