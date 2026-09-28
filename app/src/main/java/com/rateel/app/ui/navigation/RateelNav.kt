package com.rateel.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.rateel.app.R

enum class Destination(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    HOME("home", R.string.home, Icons.Outlined.Home),
    RADIO("radio", R.string.radios, Icons.Outlined.Radio),
    RECITERS("reciters", R.string.reciters, Icons.Outlined.RecordVoiceOver),
    DOWNLOADS("downloads", R.string.downloads, Icons.Outlined.Download),
    LIBRARY("library", R.string.library, Icons.Outlined.LibraryMusic),
}

@Composable
fun RateelApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val secondary = currentRoute == "settings" || currentRoute == "sources-rights" ||
        currentRoute?.startsWith("reciter/") == true || currentRoute?.startsWith("mushaf/") == true ||
        currentRoute?.startsWith("player/") == true

    Scaffold(
        bottomBar = {
            if (!secondary) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, stringResource(destination.label)) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, Destination.HOME.route, Modifier.padding(padding)) {
            composable(Destination.HOME.route) { HomeRoute(onSettings = { navController.navigate("settings") }) }
            composable(Destination.RADIO.route) { RadioRoute() }
            composable(Destination.RECITERS.route) {
                RecitersRoute(onReciter = { navController.navigate("reciter/$it") })
            }
            composable(Destination.DOWNLOADS.route) { Placeholder(R.string.downloads) }
            composable(Destination.LIBRARY.route) { Placeholder(R.string.library) }
            composable("settings") { SettingsRoute(onSources = { navController.navigate("sources-rights") }) }
            composable("sources-rights") { SourcesAndRightsRoute(onBack = { navController.popBackStack() }) }
            composable("reciter/{reciterId}") {
                ReciterDetailRoute(
                    onBack = { navController.popBackStack() },
                    onMushaf = { navController.navigate("mushaf/$it") },
                )
            }
            composable("mushaf/{mushafId}") { MushafRoute(onBack = { navController.popBackStack() }) }
            composable("player/{type}/{itemId}") { Placeholder(R.string.player) }
        }
    }
}
