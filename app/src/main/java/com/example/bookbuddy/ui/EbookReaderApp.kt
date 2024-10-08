package com.example.bookbuddy.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.bookbuddy.navigation.AppNavGraph
import com.example.bookbuddy.navigation.RouteScreen
import com.example.bookbuddy.ui.util.CustomBottomBar

@Composable
fun EbookReaderApp(navController: NavHostController, modifier: Modifier = Modifier){
    val currentScreen by navController.currentScreenAsState()
    Scaffold(
        bottomBar = {
            CustomBottomBar(
                onClick = {screen->
                    navController.navigateToRootScreen(screen)
                },
                selectedItem = currentScreen?: RouteScreen.Home
            )
        },
    ) {innerPadding->
        Box(modifier = modifier

            .padding(bottom = innerPadding.calculateBottomPadding())
            .fillMaxSize()

        ){
            AppNavGraph(navController = navController)
        }
    }
}

@Stable
@Composable
private fun NavController.currentScreenAsState(): State<RouteScreen?> {
    val selectedItem = remember { mutableStateOf<RouteScreen?>(null) }
    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.hierarchy.forEach {
                Log.d("currentScreenAsState", "currentScreenAsState: ${it.route} parent ${it.parent?.route}")
            }
            selectedItem.value = when{

               destination.hierarchy.any {it.hasRoute(RouteScreen.Home::class)}->{
                   RouteScreen.Home
               }
               destination.hierarchy.any { it.hasRoute(RouteScreen.Library::class)} ->{

                   RouteScreen.Library
               }
               destination.hierarchy.any{it.hasRoute(RouteScreen.Settings::class)} ->{
                   RouteScreen.Settings
               }
                else ->{ null}
           }
            Log.d("currentScreenAsState", "currentScreenAsState: ${selectedItem.value}")
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }
    return selectedItem
}
private fun NavController.navigateToRootScreen(rootScreen: RouteScreen) {
    navigate(rootScreen) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }

    }
}
