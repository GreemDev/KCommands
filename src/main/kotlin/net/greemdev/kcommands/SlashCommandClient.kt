package net.greemdev.kcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * A JDA [ListenerAdapter] handling [SlashCommand] checking and execution.
 * Simply add all of your [SlashCommand]s to this object (via [SlashCommandClientConfig]),
 * then add this object to JDA via [JDA.addEventListener] or [JDABuilder.addEventListeners].
 * Upon receiving the [ReadyEvent], this client will upsert every single command to Discord via HTTP.
 * Once initialization is complete, the client is ready to go.
 */
@Suppress("MemberVisibilityCanBePrivate") //I personally use the config of this client in private bots
class SlashCommandClient internal constructor(private var config: SlashCommandClientConfig = SlashCommandClientConfig.default()) :
    EventListener {

    companion object {
        private lateinit var instance: SlashCommandClient
        private lateinit var jda: JDA

        @JvmStatic infix fun get(config: SlashCommandClientConfig): SlashCommandClient {
            return tryOrNull { instance } ?: run {
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

    fun config() = config

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

        fun SlashCommand.shouldHandle(guild: Guild?) = (this is GuildSlashCommand && this.usableIn(guild?.id))

        when (event) {
            is ReadyEvent -> {
                if (isInitialized) throw IllegalStateException("Cannot reinitialize the Slash Command client.")
                event.jda withApplicationCommands config.allCommands()
                jda = event.jda
                event.jda.retrieveApplicationInfo().queue {
                    applicationOwners = if (it.team != null) {
                        arrayOf(*it.team!!.members.map { m -> m.user.id }.toTypedArray())
                    } else arrayOf(it.owner.id)

                    isInitialized = true
                }
            }

            is SlashCommandEvent -> {
                jda = event.jda
                val cmd = config.commandBy(event.name) ?: return
                if (!cmd.shouldHandle(event.guild)) return

                val ctx = SlashCommandContext(event, cmd)
                val failedChecks = cmd.runChecks(ctx)
                val result = if (failedChecks.isEmpty()) cmd.handleSlashCommand(ctx) else {
                    ctx.result {
                        withEmbed {
                            title(config.checksFailedTitle)
                            color(config.checksFailedColor)
                            description(failedChecks.joinToString("\n") {
                                config.checksFailedLineFormat.replace("{}", it.reason())
                            })
                        }
                    }
                }
                result.buildAsRestAction().queue(result.hookCallback)
            }
            is ButtonClickEvent -> {
                jda = event.jda
                val cmd = config.commandBy(event.parsedId().name() ?: event.componentId) ?: return
                if (!cmd.shouldHandle(event.guild)) return
                with (ButtonClickContext(event, cmd)) {
                    cmd.handleButtonClick(this)
                }
            }
            is SelectionMenuEvent -> {
                jda = event.jda
                val cmd = config.commandBy(event.parsedId().name() ?: event.componentId) ?: return
                if (!cmd.shouldHandle(event.guild)) return
                with (SelectionMenuContext(event, cmd)) {
                    cmd.handleSelectionMenu(this)
                }
            }
        }
    }
}