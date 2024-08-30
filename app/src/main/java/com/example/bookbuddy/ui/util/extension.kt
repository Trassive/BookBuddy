package com.example.bookbuddy.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import kotlin.reflect.KClass


inline fun <reified T : Any> Any.isOneOf(vararg types: KClass<out T>): Boolean {
    return types.any { type -> type.isInstance(this) }
}
fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}
fun Modifier.conditionally(
    condition: Boolean,
    modifier: Modifier
): Modifier {
    return if (condition) {
        this.then(modifier)
    } else {
        this
    }
}

