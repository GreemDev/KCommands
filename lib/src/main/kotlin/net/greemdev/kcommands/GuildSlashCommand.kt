package net.greemdev.kcommands

abstract class GuildSlashCommand(val guildId: String, name: String, description: String) : SlashCommand(name, description)