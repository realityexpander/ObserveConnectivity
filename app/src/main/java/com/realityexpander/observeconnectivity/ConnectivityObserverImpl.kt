package com.realityexpander.observeconnectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityObserverImpl(
    private val context: Context
): ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // use connectivityManager.requestNetwork(networkRequest, networkCallback) for api levels lower than 24

    override fun observe(): Flow<ConnectivityObserver.Status> {

        return callbackFlow {

            val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        trySend(ConnectivityObserver.Status.Available)
                        //launch { send(ConnectivityObserver.Status.Available) } // can also use `offer`
                    }

                    override fun onLosing(network: Network, maxMsToLive: Int) {
                        super.onLosing(network, maxMsToLive)
                        trySend(ConnectivityObserver.Status.Losing)
                        //launch { send(ConnectivityObserver.Status.Losing) }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        trySend(ConnectivityObserver.Status.Lost)
                        //launch { send(ConnectivityObserver.Status.Lost) }
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        trySend(ConnectivityObserver.Status.Unavailable)
                        //launch { send(ConnectivityObserver.Status.Unavailable) }
                    }
                }

            // Start listening for network changes
            connectivityManager.registerDefaultNetworkCallback(callback)

            // closes only when the scope that launched it is cancelled (e.g. when the activity/fragment is destroyed)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}