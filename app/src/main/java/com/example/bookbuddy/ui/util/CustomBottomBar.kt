package com.example.bookbuddy.ui.util

 import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bookbuddy.R
import com.example.bookbuddy.model.NavigationItem
import com.example.bookbuddy.navigation.RouteScreen
import com.example.compose.BookBuddyTheme
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.StraightIndent
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.exyte.animatednavbar.items.dropletbutton.DropletButton

@Composable
fun CustomBottomBar(
    onClick: (RouteScreen)-> Unit,
    selectedItem: RouteScreen
) {
    val navigationItems = listOf(
        NavigationItem(
            screen = RouteScreen.Home,
            icon = R.drawable.round_home_24
        ),
        NavigationItem(
            screen = RouteScreen.Library,
            icon = R.drawable.round_local_library_24
        ),
        NavigationItem(
            screen = RouteScreen.Settings,
            icon = R.drawable.round_settings_24
        )
    )
    Log.d("BottomBar selectedItem",selectedItem.index().toString())
    AnimatedNavigationBar(
        modifier = Modifier
            .height(75.dp),
        selectedIndex = selectedItem.index(),
        ballColor = Color(0xFFDAAA63),
        cornerRadius = shapeCornerRadius(40.dp),
        barColor = Color(0xFFDAAA63),
        ballAnimation = Parabolic(tween(800, easing = LinearOutSlowInEasing)),
        indentAnimation = StraightIndent(
            indentWidth = 56.dp,
            indentHeight = 15.dp,
            animationSpec = tween(800)
        )
    ) {
        navigationItems.forEach { navigationItem ->
            DropletButton(
                modifier = Modifier.fillMaxSize(),
                isSelected = selectedItem == navigationItem.screen,
                onClick = { onClick(navigationItem.screen) },
                iconColor = Color.White,
                icon = navigationItem.icon,
                dropletColor = Color(0xFF2C1810),
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            )
        }
    }
}
private fun RouteScreen.index(): Int = when(this){
        RouteScreen.Home -> 0
        RouteScreen.Library->1
        RouteScreen.Settings->2
}
@Preview
@Composable
fun BottomBarPreview(){
    BookBuddyTheme {
        var selectedItem:RouteScreen by remember{ mutableStateOf( RouteScreen.Home) }

        Scaffold(bottomBar = { CustomBottomBar(onClick = {selectedItem = it}, selectedItem = selectedItem) }){
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(it)){

            }
        }
    }
}