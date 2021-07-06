package net.greemdev.kcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.greemdev.kcommands.ext.parsedId
import net.greemdev.kcommands.ext.withApplicationCommands
import net.greemdev.kcommands.obj.ButtonClickContext
import net.greemdev.kcommands.obj.SlashCommandContext
import net.greemdev.kcommands.util.executeElseNull

/**
 * A JDA [ListenerAdapter] handling [SlashCommand] checking and execution.
 * Simply add all of your [SlashCommand]s to this object (via [SlashCommandClientConfig]),
 * then add this object to JDA via [JDA.addEventListener] or [JDABuilder.addEventListeners].
 * Upon receiving the [ReadyEvent], this client will upsert every single command to Discord via HTTP.
 * Once initialization is complete, the client is ready to go.
 */
@Suppress("MemberVisibilityCanBePrivate") //I personally use the config of this client in private bots
class SlashCommandClient internal constructor(var config: SlashCommandClientConfig = SlashCommandClientConfig.default()) :
    EventListener {

    companion object {
        private lateinit var instance: SlashCommandClient

        @JvmStatic infix fun get(config: SlashCommandClientConfig): SlashCommandClient {
            return executeElseNull { instance } ?: run {
                instance = SlashCommandClient(config)
                instance
            }
        }

        @JvmStatic infix fun get(initializer: SlashCommandClientConfig.() -> Unit): SlashCommandClient {
            return get(SlashCommandClientConfig.of(initializer))
        }

        @JvmStatic fun get(commands: Set<SlashCommand> = hashSetOf()) = get(SlashCommandClientConfig.justCommands(commands))
    }

    internal var applicationOwners = arrayOf<String>()

    /**
     * Whether or not the current [SlashCommandClient] has had its commands upserted to Discord and is ready to receive command events.
     */
    @Suppress("RedundantSetter", "RedundantGetter")
    var isInitialized = false
        get() { return field }
        private set(value) {
            field = value
        }

    override fun onEvent(event: GenericEvent) {
        when (event::class) {
            ReadyEvent::class -> { event as ReadyEvent
                if (isInitialized) throw IllegalStateException("Cannot reinitialize the Slash Command client.")
                event.jda withApplicationCommands config.commands
                isInitialized = true
                event.jda.retrieveApplicationInfo().queue {
                    applicationOwners = hashSetOf<String>().apply {
                        if (it.team != null) {
                            addAll(it.team!!.members.map { m -> m.user.id })
                        } else {
                            add(it.owner.id)
                        }
                    }.toTypedArray()
                }
            }

            SlashCommandEvent::class -> { event as SlashCommandEvent
                val cmd = config.commands.firstOrNull { it.name == event.name } ?: return
                if (cmd is GuildSlashCommand && cmd.guildId != event.guild?.id) return

                val ctx = SlashCommandContext(event, cmd)
                val failedChecks = cmd.checks
                    .filter { !it.check(ctx) }
                if (failedChecks.isEmpty()) {
                    cmd.handleSlashCommand(ctx)
                } else {
                    ctx.reply {
                        title(config.checksFailedTitle)
                        color(config.checksFailedColor)
                        description(failedChecks.joinToString("\n") {
                            config.checksFailedLineFormat.replace("{}", it.reason())
                        })
                    }.queue()
                }
            }
            ButtonClickEvent::class -> { event as ButtonClickEvent
                val cmd = config.commands.firstOrNull { it.name == (event.parsedId().name() ?: event.componentId) } ?: return
                val ctx = ButtonClickContext(event, cmd)
                if (!(cmd is GuildSlashCommand && cmd.guildId != event.guild?.id))
                    cmd.handleButtonClick(ctx)
            }
        }
    }
}