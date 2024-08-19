package com.example.bookbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.bookbuddy.ui.homescreen.HomeScreen


@Composable
fun AppNavGraph(navController: NavHostController){
    NavHost(navController = navController, startDestination = RouteScreen.Home){
        addHomeRoute(navController)
        addLibraryRoute(navController)
        addSettingsRoute(navController)
    }
}

fun NavGraphBuilder.addHomeRoute(navController: NavHostController) {
    navigation<RouteScreen.Home>(startDestination = LeafScreen.Home){
        composable<LeafScreen.Home> {

        }
        commonScreen(navController)
    }
}
fun NavGraphBuilder.addLibraryRoute(navController: NavHostController){
    navigation<RouteScreen.Library>(startDestination = LeafScreen.Library){

    }
}
fun NavGraphBuilder.addSettingsRoute(navController: NavHostController){
    navigation<RouteScreen.Settings>(startDestination = LeafScreen.Settings){

    }
}
fun NavGraphBuilder.commonScreen(navController: NavHostController){
    composable<LeafScreen.TableOfContent>{backStackEntry ->
        val toc: LeafScreen.TableOfContent = backStackEntry.toRoute()
    }
    composable<LeafScreen.Library>{ backStackEntry ->
        val reader: LeafScreen.Library = backStackEntry.toRoute()
    }
}