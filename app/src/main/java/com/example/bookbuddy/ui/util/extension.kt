package com.example.bookbuddy.ui.util

import kotlin.reflect.KClass


inline fun <reified T : Any> Any.isOneOf(vararg types: KClass<out T>): Boolean {
    return types.any { type -> type.isInstance(this) }
}


