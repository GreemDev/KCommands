@file:JvmName("CoreUtil")
package net.greemdev.kcommands

fun <V> executeElseNull(func: () -> V): V? = try { func() } catch (t: Throwable) { null }

fun markdown(value: String) = Markdown(value)

data class Markdown internal constructor(var value: String) {
    private fun String.surround(with: String) = "$with$this$with"

    fun bold() = value.surround("**")
    fun italicize() = value.surround("*")
    fun spoiler() = value.surround("||")
    fun underline() = value.surround("__")
    fun inlineCode() = value.surround("`")
    fun blockCode(lang: String = "") = "${if (lang.isNotEmpty()) "$lang\n" else ""}${value}".surround("```")
}