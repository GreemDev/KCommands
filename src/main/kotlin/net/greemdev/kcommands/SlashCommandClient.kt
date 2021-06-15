package net.greemdev.kcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.greemdev.kcommands.ext.withApplicationCommands
import net.greemdev.kcommands.obj.ButtonClickContext
import net.greemdev.kcommands.obj.SlashCommandContext

/**
 * A JDA [ListenerAdapter] handling [SlashCommand] checking and execution.
 * Simply add all of your [SlashCommand]s to this object, then add this object to JDA via [JDA.addEventListener] or [JDABuilder.addEventListeners].
 * Upon receiving the [ReadyEvent], this client will upsert every single command to Discord via HTTP.
 * Once this has been done, a log message will be printed describing what commands were uploaded because they're new. If no commands are new nothing will happen.
 */
class SlashCommandClient constructor(private var config: SlashCommandClientConfig = SlashCommandClientConfig()) :
    ListenerAdapter() {

    companion object {
        private lateinit var instance: SlashCommandClient

        @JvmStatic infix fun get(config: SlashCommandClientConfig): SlashCommandClient {
            return try {
                instance
            } catch (e: UninitializedPropertyAccessException) {
                instance = SlashCommandClient(config)
                instance
            }
        }

        @JvmStatic infix fun get(initializer: SlashCommandClientConfig.() -> Unit): SlashCommandClient {
            return get(SlashCommandClientConfig of initializer)
        }

        @JvmStatic fun get(commands: Set<SlashCommand> = hashSetOf()) = get(SlashCommandClientConfig justCommands commands)
    }

    /**
     * Whether or not the current [SlashCommandClient] has had its commands upserted to Discord and is ready to receive command events.
     */
    var isInitialized = false

    private fun init(jda: JDA) {
        jda withApplicationCommands config.commands
        isInitialized = true
    }

    override fun onReady(event: ReadyEvent) = init(event.jda)

    override fun onSlashCommand(event: SlashCommandEvent) {
        val cmd = config.commands.firstOrNull { it.name == event.name } ?: return
        if (cmd is GuildSlashCommand && cmd.guildId != event.guild?.id) return

        val ctx = SlashCommandContext(event, cmd)
        val failedCheck = cmd.checks.firstOrNull { !it.check(ctx) }
        if (failedCheck == null) {
            cmd.handleSlashCommand(ctx)
        } else {
            ctx.reply {
                title(config.checkFailedTitle)
                color(config.checkFailedColor)
                description(failedCheck.reason())
            }.queue()
        }
    }

    override fun onButtonClick(event: ButtonClickEvent) {
        val ctx = ButtonClickContext(event)
        val cmd = config.commands.firstOrNull { it.name == (ctx.parsedComponent().name() ?: event.componentId) } ?: return
        if (cmd is GuildSlashCommand && cmd.guildId != event.guild?.id) return

        cmd.handleButtonClick(ctx)
    }
}