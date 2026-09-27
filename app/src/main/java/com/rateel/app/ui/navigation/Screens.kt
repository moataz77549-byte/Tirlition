package com.rateel.app.ui.navigation
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rateel.app.R
@OptIn(ExperimentalMaterial3Api::class) @Composable fun HomeScreen(onSettings:()->Unit){Scaffold(topBar={TopAppBar(title={Text(stringResource(R.string.app_name))},actions={IconButton(onClick=onSettings){Icon(Icons.Outlined.Settings,stringResource(R.string.settings))}})}){p->Column(Modifier.padding(p).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("استمع بقلب حاضر",style=MaterialTheme.typography.headlineMedium);Card{Column(Modifier.padding(20.dp)){Text("محتوى رتيل",style=MaterialTheme.typography.titleLarge);Text("الإذاعات والقراء والمصاحف الصوتية ستظهر هنا عبر طبقة البيانات دون ربط الواجهة بمصدر محدد.")}}}}}
@OptIn(ExperimentalMaterial3Api::class) @Composable fun Placeholder(@StringRes title:Int){Scaffold(topBar={TopAppBar(title={Text(stringResource(title))})}){p->Box(Modifier.fillMaxSize().padding(p),contentAlignment=Alignment.Center){Text(stringResource(R.string.coming_soon))}}}
