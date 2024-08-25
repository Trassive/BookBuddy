package com.example.bookbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.bookbuddy.ui.homescreen.HomeScreen
import com.example.bookbuddy.ui.homescreen.HomeScreenViewModel
import com.example.bookbuddy.ui.library.LibraryScreen
import com.example.bookbuddy.ui.library.LibraryScreenViewModel
import com.example.bookbuddy.ui.tableofcontent.ContentViewModel
import com.example.bookbuddy.ui.tableofcontent.TableOfContentScreen


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
            val viewModel = hiltViewModel<HomeScreenViewModel>()
            HomeScreen(
                viewModel = viewModel,
                onClick = {id->
                navController.navigate(LeafScreen.BookDetail(id))
                }
            )
        }
        commonScreen(navController)
    }
}
fun NavGraphBuilder.addLibraryRoute(navController: NavHostController){
    navigation<RouteScreen.Library>(startDestination = LeafScreen.Library){
        composable<LeafScreen.Library> {
            val viewModel = hiltViewModel<LibraryScreenViewModel>()
            LibraryScreen(viewModel = viewModel, onClick = {id->
                navController.navigate(LeafScreen.BookDetail(id))
            })
        }
    }
}
fun NavGraphBuilder.addSettingsRoute(navController: NavHostController){
    navigation<RouteScreen.Settings>(startDestination = LeafScreen.Settings){

    }
}
fun NavGraphBuilder.commonScreen(navController: NavHostController){
    composable<LeafScreen.TableOfContent>{backStackEntry ->
        val viewModel = hiltViewModel<ContentViewModel>()

    }
    composable<LeafScreen.Library>{ backStackEntry ->
        val reader: LeafScreen.Library = backStackEntry.toRoute()
    }
}