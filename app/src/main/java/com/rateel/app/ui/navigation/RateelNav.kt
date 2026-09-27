package com.rateel.app.ui.navigation
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
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
enum class Destination(val route:String,@StringRes val label:Int,val icon:ImageVector){HOME("home",R.string.home,Icons.Outlined.Home),RADIO("radio",R.string.radios,Icons.Outlined.Radio),RECITERS("reciters",R.string.reciters,Icons.Outlined.RecordVoiceOver),DOWNLOADS("downloads",R.string.downloads,Icons.Outlined.Download),LIBRARY("library",R.string.library,Icons.Outlined.LibraryMusic)}
@Composable fun RateelApp(){val nav=rememberNavController();val back by nav.currentBackStackEntryAsState();val route=back?.destination?.route
 Scaffold(bottomBar={NavigationBar{Destination.entries.forEach{d->NavigationBarItem(selected=route==d.route,onClick={nav.navigate(d.route){popUpTo(nav.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(d.icon,null)},label={Text(stringResource(d.label))})}}}){pad->
  NavHost(nav,startDestination=Destination.HOME.route,Modifier.padding(pad)){composable("home"){HomeScreen(onSettings={nav.navigate("settings")})};composable("radio"){Placeholder(R.string.radios)};composable("reciters"){Placeholder(R.string.reciters)};composable("downloads"){Placeholder(R.string.downloads)};composable("library"){Placeholder(R.string.library)};composable("settings"){Placeholder(R.string.settings)}}
 }}
