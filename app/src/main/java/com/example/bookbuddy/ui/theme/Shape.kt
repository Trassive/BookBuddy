package com.example.bookbuddy.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AppShapes {
    private val extraSmall = 4.dp
    private val small = 8.dp
    private val medium = 16.dp
    private val large = 24.dp
    private val extraLarge = 32.dp

    val extraSmallShape = RoundedCornerShape(extraSmall)
    val smallShape = RoundedCornerShape(small)
    val mediumShape = RoundedCornerShape(medium)
    val largeShape = RoundedCornerShape(large)
    val extraLargeShape = RoundedCornerShape(extraLarge)

    // Shapes with different corner sizes
    val Shapes.topRoundedLarge
        get(): CornerBasedShape  = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    val Shapes.bottomRoundedLarge
        get(): CornerBasedShape = RoundedCornerShape(bottomEnd = 32.dp, bottomStart = 32.dp)

    // Material 3 Shapes
    val shapes = Shapes(
        extraSmall = extraSmallShape,
        small = smallShape,
        medium = mediumShape,
        large = largeShape,
        extraLarge = extraLargeShape
    )

    // Custom shapes for specific use cases
    val buttonShape = smallShape
    val cardShape = mediumShape

    val dialogShape = mediumShape
}
