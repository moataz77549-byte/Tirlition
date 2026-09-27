package com.rateel.app.core.network
import android.content.Context
import android.net.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
class NetworkMonitor(context:Context){private val cm=context.getSystemService(ConnectivityManager::class.java);val connected=callbackFlow{fun state()=cm.activeNetwork?.let{cm.getNetworkCapabilities(it)}?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)==true;trySend(state());val cb=object:ConnectivityManager.NetworkCallback(){override fun onAvailable(n:Network){trySend(state())};override fun onLost(n:Network){trySend(state())};override fun onCapabilitiesChanged(n:Network,c:NetworkCapabilities){trySend(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))}};cm.registerDefaultNetworkCallback(cb);awaitClose{cm.unregisterNetworkCallback(cb)}}}
