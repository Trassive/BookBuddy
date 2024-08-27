@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bookbuddy.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.detailscreen.DetailScreen
import com.example.bookbuddy.ui.detailscreen.DetailScreenViewModel
import com.example.bookbuddy.ui.homescreen.HomeScreen
import com.example.bookbuddy.ui.homescreen.HomeScreenViewModel
import com.example.bookbuddy.ui.library.LibraryScreen
import com.example.bookbuddy.ui.library.LibraryScreenViewModel
import com.example.bookbuddy.ui.readerscreen.ReaderScreen
import com.example.bookbuddy.ui.readerscreen.ReaderViewModel
import com.example.bookbuddy.ui.tableofcontent.ContentViewModel
import com.example.bookbuddy.ui.tableofcontent.TableOfContentScreen
import com.example.bookbuddy.ui.util.LottieAnimationComposable


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
            LibraryScreen(
                viewModel = viewModel,
                onClick = {id->
                navController.navigate(LeafScreen.BookDetail(id))
                }
            )
        }
        commonScreen(navController)
    }
}
fun NavGraphBuilder.addSettingsRoute(navController: NavHostController){
    navigation<RouteScreen.Settings>(startDestination = LeafScreen.Settings){
        composable<LeafScreen.Settings>{
            LottieAnimationComposable(id = R.raw.loading2, modifier = Modifier.fillMaxSize())
        }
    }
}
fun NavGraphBuilder.commonScreen(navController: NavHostController){
    composable<LeafScreen.TableOfContent>{ 
        val viewModel = hiltViewModel<ContentViewModel>()
        TableOfContentScreen(
            viewModel = viewModel,
            goTo = { id, link-> navController.navigate(LeafScreen.Reader(id ,link))},
            onArrow = { navController.navigateUp()}
        )
    }
    composable<LeafScreen.BookDetail>{
        val viewModel = hiltViewModel<DetailScreenViewModel>()
        DetailScreen(
            detailScreenViewModel = viewModel,
            onArrow = { navController.navigateUp()},
            onClick = {id -> navController.navigate(LeafScreen.TableOfContent(id))}
        )
    }
    composable<LeafScreen.Reader>{   
        val viewModel = hiltViewModel<ReaderViewModel>()
        ReaderScreen(viewModel = viewModel)
    }
}