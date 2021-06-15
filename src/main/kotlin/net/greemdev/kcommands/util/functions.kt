@file:JvmName("CoreUtil")
package net.greemdev.kcommands.util

fun<T> executeElseNull(func: () -> T): T? = try { func() } catch (t: Throwable) { null }