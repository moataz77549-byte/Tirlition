package com.rateel.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.hilt.navigation.compose.hiltViewModel
import com.rateel.app.feature.player.PlayerViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.net.Uri
import com.rateel.app.R

enum class Destination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.home, Icons.Outlined.Home),
    RADIO("radio", R.string.radios, Icons.Outlined.Radio),
    RECITERS("reciters", R.string.reciters, Icons.Outlined.RecordVoiceOver),
    DOWNLOADS("downloads", R.string.downloads, Icons.Outlined.Download),
    LIBRARY("library", R.string.library, Icons.Outlined.LibraryMusic),
}

@Composable
fun RateelApp() {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val secondaryRoutes = setOf("settings", "sources-rights")

    Scaffold(
        bottomBar = {
            Column {
            if (currentRoute != "player") {
                MiniPlayer(onOpen = { navController.navigate("player") }, vm = playerViewModel)
            }
            if (currentRoute !in secondaryRoutes && currentRoute != "player" && currentRoute?.startsWith("reciter/") != true &&
                currentRoute?.startsWith("mushaf/") != true && currentRoute?.startsWith("radio/") != true &&
                currentRoute?.startsWith("surah/") != true) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = stringResource(destination.label),
                                )
                            },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.HOME.route) {
                HomeRoute(onSettings = { navController.navigate("settings") })
            }
            composable(Destination.RADIO.route) {
                RadiosRoute(onStation = { navController.navigate("radio/${Uri.encode(it)}") })
            }
            composable(Destination.RECITERS.route) {
                RecitersRoute(onReciter = { navController.navigate("reciter/${Uri.encode(it)}") })
            }
            composable("radio/{id}") { entry ->
                RadioDetailRoute(
                    id = Uri.decode(entry.arguments?.getString("id").orEmpty()),
                    onBack = { navController.popBackStack() },
                )
            }
            composable("reciter/{id}") { entry ->
                MushafsRoute(
                    id = Uri.decode(entry.arguments?.getString("id").orEmpty()),
                    onBack = { navController.popBackStack() },
                    onMushaf = { navController.navigate("mushaf/${Uri.encode(it)}") },
                )
            }
            composable("mushaf/{id}") { entry ->
                SurahsRoute(
                    id = Uri.decode(entry.arguments?.getString("id").orEmpty()),
                    onBack = { navController.popBackStack() },
                    onSurah = { navController.navigate("surah/${Uri.encode(it)}") },
                )
            }
            composable("surah/{id}") { entry ->
                SurahDetailRoute(
                    id = Uri.decode(entry.arguments?.getString("id").orEmpty()),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.DOWNLOADS.route) { DownloadsRoute() }
            composable(Destination.LIBRARY.route) { LibraryRoute(onSurah = { navController.navigate("surah/${Uri.encode(it)}") }) }
            composable("settings") {
                SettingsRoute(onSources = { navController.navigate("sources-rights") })
            }
            composable("sources-rights") {
                SourcesAndRightsRoute(onBack = { navController.popBackStack() })
            }
            composable("player") {
                FullPlayerRoute(onBack = { navController.popBackStack() }, vm = playerViewModel)
            }
        }
    }
}
