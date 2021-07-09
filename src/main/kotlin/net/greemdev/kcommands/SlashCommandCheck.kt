package net.greemdev.kcommands

data class SlashCommandCheck(var check: (SlashCommandContext) -> Boolean = {true}) {

    private var failureReason: String = "no reason provided"

    fun predicate(check: (SlashCommandContext) -> Boolean) {
        this.check = check
    }

    fun reason(): String = failureReason
    fun reason(reason: String) {
        failureReason = reason
    }

    constructor(check: (SlashCommandContext) -> Boolean, initializer: SlashCommandCheck.() -> Unit) : this(check) {
        initializer()
    }
}