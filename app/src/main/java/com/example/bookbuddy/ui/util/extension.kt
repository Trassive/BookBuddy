package com.example.bookbuddy.ui.util

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.reflect.KClass

fun Modifier.parallaxLayoutModifier(scrollState: ScrollState, rate: Int) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val height = if (rate > 0) scrollState.value / rate else scrollState.value
        layout(placeable.width, placeable.height) {
            placeable.place(0, height)
        }
    }
inline fun <reified T : Any> Any.isOneOf(vararg types: KClass<out T>): Boolean {
    return types.any { type -> type.isInstance(this) }
}


